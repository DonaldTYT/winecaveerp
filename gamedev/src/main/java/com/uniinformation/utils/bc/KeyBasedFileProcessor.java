package com.uniinformation.utils.bc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.util.io.Streams;

import com.google.api.client.util.IOUtils;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;

/**
 * A simple utility class that encrypts/decrypts public key based
 * encryption files.
 * <p>
 * To encrypt a file: KeyBasedFileProcessor -e [-a|-ai] fileName publicKeyFile.<br>
 * If -a is specified the output file will be "ascii-armored".
 * If -i is specified the output file will be have integrity checking added.
 * <p>
 * To decrypt: KeyBasedFileProcessor -d fileName secretKeyFile passPhrase.
 * <p>
 * Note 1: this example will silently overwrite files, nor does it pay any attention to
 * the specification of "_CONSOLE" in the filename. It also expects that a single pass phrase
 * will have been used.
 * <p>
 * Note 2: if an empty file name has been specified in the literal data object contained in the
 * encrypted packet a file with the name filename.out will be generated in the current working directory.
 */
public class KeyBasedFileProcessor
{
    public static void decryptFile(
        String inputFileName,
        String keyFileName,
        char[] passwd,
        String defaultFileName)
        throws IOException, NoSuchProviderException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(inputFileName));
        InputStream keyIn = new BufferedInputStream(new FileInputStream(keyFileName));
        decryptFile(in, keyIn, passwd, defaultFileName);
        keyIn.close();
        in.close();
    }

    /**
     * decrypt the passed in message stream
     */
    private static void decryptFile(
        InputStream in,
        InputStream keyIn,
        char[]      passwd,
        String      defaultFileName)
        throws IOException, NoSuchProviderException
    {
        in = PGPUtil.getDecoderStream(in);
        
        try
        {
            JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(in);
            PGPEncryptedDataList    enc;

            Object                  o = pgpF.nextObject();
            //
            // the first object might be a PGP marker packet.
            //
            if (o instanceof PGPEncryptedDataList)
            {
                enc = (PGPEncryptedDataList)o;
            }
            else
            {
                enc = (PGPEncryptedDataList)pgpF.nextObject();
            }
            
            //
            // find the secret key
            //
            Iterator                    it = enc.getEncryptedDataObjects();
            PGPPrivateKey               sKey = null;
            PGPPublicKeyEncryptedData   pbe = null;
            PGPSecretKeyRingCollection  pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

            while (sKey == null && it.hasNext())
            {
                pbe = (PGPPublicKeyEncryptedData)it.next();
                UniLog.log1("keyid:%d %X", pbe.getKeyID(), pbe.getKeyID());
                
                sKey = PGPExampleUtil.findSecretKey(pgpSec, pbe.getKeyID(), passwd);
            }
            
            if (sKey == null)
            {
                throw new IllegalArgumentException("secret key for message not found.");
            }
    
            InputStream         clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));
            
            JcaPGPObjectFactory    plainFact = new JcaPGPObjectFactory(clear);
            
            Object              message = plainFact.nextObject();
    
            JcaPGPObjectFactory pgpFact = null;
            if (message instanceof PGPCompressedData)
            {
                PGPCompressedData   cData = (PGPCompressedData)message;
                pgpFact = new JcaPGPObjectFactory(cData.getDataStream());
                
                message = pgpFact.nextObject();
            }
            
            if (message instanceof PGPLiteralData)
            {
                PGPLiteralData ld = (PGPLiteralData)message;

                /*
                String outFileName = ld.getFileName();
                if (outFileName.length() == 0)
                {
                    outFileName = defaultFileName;
                }
                */
                //andrew210518 force use custom file
                String outFileName = defaultFileName;

                InputStream unc = ld.getInputStream();
                OutputStream fOut = new BufferedOutputStream(new FileOutputStream(outFileName));

                Streams.pipeAll(unc, fOut);

                fOut.close();
            }
            else if (message instanceof PGPOnePassSignatureList)
            {
                //throw new PGPException("encrypted message contains a signed message - not literal data.");
            	//andrew210524 bypass signature validation
            	PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) message;
                PGPOnePassSignature ops = p1.get(0);
               
            	if (pgpFact == null) {
            		throw new PGPException("pgpFact is null");
            	}
                PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();
                InputStream unc = p2.getInputStream();
                String outFileName = defaultFileName;
                OutputStream fOut = new BufferedOutputStream(new FileOutputStream(outFileName));
                Streams.pipeAll(unc, fOut);
                fOut.close();
            }
            else
            {
                throw new PGPException("message is not a simple encrypted file - type unknown.");
            }

            if (pbe.isIntegrityProtected())
            {
                if (!pbe.verify())
                {
                    System.err.println("message failed integrity check");
                }
                else
                {
                    System.err.println("message integrity check passed");
                }
            }
            else
            {
                System.err.println("no message integrity check");
            }
        }
        catch (PGPException e)
        {
            System.err.println(e);
            if (e.getUnderlyingException() != null)
            {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }

    public static void encryptFile(
        String          outputFileName,
        String          inputFileName,
        String          encKeyFileName,
        boolean         armor,
        boolean         withIntegrityCheck)
        throws IOException, NoSuchProviderException, PGPException
    {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFileName));
        PGPPublicKey encKey = PGPExampleUtil.readPublicKey(encKeyFileName);
        encryptFile(out, inputFileName, encKey, armor, withIntegrityCheck);
        out.close();
    }

    private static void encryptFile(
        OutputStream    out,
        String          fileName,
        PGPPublicKey    encKey,
        boolean         armor,
        boolean         withIntegrityCheck)
        throws IOException, NoSuchProviderException
    {
        if (armor)
        {
            out = new ArmoredOutputStream(out);
        }

        try
        {
            byte[] bytes = PGPExampleUtil.compressFile(fileName, CompressionAlgorithmTags.ZIP);

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5).setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider("BC"));

            encGen.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider("BC"));

            OutputStream cOut = encGen.open(out, bytes.length);

            cOut.write(bytes);
            cOut.close();

            if (armor)
            {
                out.close();
            }
        }
        catch (PGPException e)
        {
            System.err.println(e);
            if (e.getUnderlyingException() != null)
            {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }
    /*
    public static List<File> axaDecFile(String p_priKeyFile, String p_priPass, String p_inFile, String p_outFolder) throws Exception{
        Security.addProvider(new BouncyCastleProvider());
        File tmpDecFile = File.createTempFile("axadec", ".tmp",new File("/tmp"));
        UniLog.log1("tmpDecFile %s",tmpDecFile);
        decryptFile(p_inFile, p_priKeyFile, p_priPass.toCharArray(), tmpDecFile.getAbsolutePath());
        List<File> outFiles = ZipUtil.untar(tmpDecFile, new File(p_outFolder));
        if (outFiles != null && outFiles.size() > 0) {
        	//delete tmpfile for normal case
        	tmpDecFile.delete();
        }
        return outFiles;
    }
    public static ReturnMsg axaEncFile(String p_pubKeyFile, List<Pair<File,String>> p_inFiles, String p_outFile) {
        Security.addProvider(new BouncyCastleProvider());
        try {
        	UniLog.log("before enc");
        	File tmpDecFile = File.createTempFile("axaenc", ".tmp",new File("/tmp"));
        	ZipUtil.tar(p_inFiles, tmpDecFile);
        	encryptFile(p_outFile, tmpDecFile.getAbsolutePath(), p_pubKeyFile, true, true);
        	UniLog.log("after enc");
        	return ReturnMsg.defaultOk;
        }
        catch(Exception ex) {
        	ex.printStackTrace();
        	return new ReturnMsg(ex);
        }
    }
    */

    public static void main( String[] args) throws Exception
    {
    	//axaEncFile("/tmp/pgp/axatest-pub-001.asc", "/tmp/pgp/a.txt", "/tmp/pgp/a.txt.enc"); //use our our own pubkey for testing
    	//axaEncFile("/tmp/pgp/PEDDpanel.txt", Arrays.asList(Pair.of(new File("/tmp/pgp/a.txt"),"a1.txt")), "/tmp/pgp/a.txt.enc"); //use axa pubkey
    	//axaDecFile("/tmp/pgp/axatest-pri-001.asc", "pass001", "/tmp/pgp/PEDD2.pgp", "/tmp/pgp/out");

        if (true) return;
        /*

        if (args.length == 0)
        {
            System.err.println("usage: KeyBasedFileProcessor -e|-d [-a|ai] file [secretKeyFile passPhrase|pubKeyFile]");
            return;
        }

        if (args[0].equals("-e"))
        {
            if (args[1].equals("-a") || args[1].equals("-ai") || args[1].equals("-ia"))
            {
                encryptFile(args[2] + ".asc", args[2], args[3], true, (args[1].indexOf('i') > 0));
            }
            else if (args[1].equals("-i"))
            {
                encryptFile(args[2] + ".bpg", args[2], args[3], false, true);
            }
            else
            {
                encryptFile(args[1] + ".bpg", args[1], args[2], false, false);
            }
        }
        else if (args[0].equals("-d"))
        {
            decryptFile(args[1], args[2], args[3].toCharArray(), new File(args[1]).getName() + ".out");
        }
        else
        {
            System.err.println("usage: KeyBasedFileProcessor -d|-e [-a|ai] file [secretKeyFile passPhrase|pubKeyFile]");
        }
        */
    }
}