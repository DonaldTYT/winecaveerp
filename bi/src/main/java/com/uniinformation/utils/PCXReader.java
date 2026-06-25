package com.uniinformation.utils;

import java.io.InputStream;

public class PCXReader {
	// Data field, place holder, the real values are to be set by a subclass
    int width = 0;
	int height = 0;
	int pix[] = null;	
	int bitsPerPixel = 0;
    // IndexedColor related variables 
	boolean indexedColor =  false;
    int colorPalette[] = null;
    // End of indexedColor related variables

	short bytesPerLine = 0;
	byte NPlanes = 0;
	PcxHeader pcxHeader;
	
	
	//Get the color depth of the image to determine whether a colorPalette exists
	public int getColorDepth()
	{
	   return bitsPerPixel;
	}
    //Returns a colorPalette for an indexed-color image
	public int[] getColorPalette()
	{
       return colorPalette; // call this method if getImageColorDepth() <= 8
	}
	// Is it an indexedColor image
	public boolean isIndexedColor()
	{
		return indexedColor;
	}
    //Returns the Dimension of the image
    public int getImageWidth()
    {
       return width;
    }
    public int getImageHeight()
    {
       return height;
    }
    //Returns an integer array representation of the image 
  	//in alpha_RGB sequence
	public int[] getImageData()
    {
	   return pix;// check about null pointer after method call ... 
    }

   /** This method could have returned an Image object directly using
     * Toolkit.getDefaultToolkit().createImage(ImageProducer) method, 
     * but this method is somewhat slow when called in a non-gui class in
     * creating an image, a subclass of JFrame would do much faster!
	 */
  
    public void unpackImage(InputStream is) throws Exception
    {
		pcxHeader = new PcxHeader();
      	pcxHeader.readHeader(is);
		width = pcxHeader.xmax-pcxHeader.xmin+1;
	    height = pcxHeader.ymax-pcxHeader.ymin+1;
		bytesPerLine = pcxHeader.bytes_per_line;
		colorPalette = pcxHeader.colorPalette;
		NPlanes = pcxHeader.color_plane;

		indexedColor = true;
		
	    pix = new int[width*height];

		bitsPerPixel = pcxHeader.bits_per_pixel*pcxHeader.color_plane;

		if((pcxHeader.bits_per_pixel == 8)&&(pcxHeader.color_plane == 1))
		{
		    unpack256ColorPcxFile(is);
		}
		else if((pcxHeader.bits_per_pixel == 8)&&(pcxHeader.color_plane == 3))
		{
    	    unpackTrueColorPcxFile(is);
		}
		else 
	    {
			if (pcxHeader.bits_per_pixel == 1)
			{
				switch (NPlanes)
				{
				  case 4: 
					  System.out.println("16 color image!");
				      break;
				  case 3:
                      System.out.println("8 color image!");
				      break;
				  case 2:
                      System.out.println("4 color image!");
				      break;
				  case 1:
					  System.out.println("black and white image!");
				      break;

                  default: 
				}
				unpackOneBitEgaPcxFile(is);
			}
			else if (NPlanes == 1)
			{
				switch (bitsPerPixel)
				{
				  case 4: 
					  System.out.println("...16 color pcx image..."
					                    +"\n4 bits per pixel, 1 color plane!");
				      break;
				  case 3:
                      System.out.println("...8 color pcx image not implemented..."
				                        +"\n3 bits per pixel, 1 color plane");
					  break;
				  case 2:
                      System.out.println("...4 color pcx image..."
				                        +"\n2 bits per pixel, 1 color plane!");
				      break;
                  default: 
				}
				unpackOnePlaneEgaPcxFile(is);
			}
			else
			{   //hope this will never happen
                System.out.println("unimplemented for this format!");
			}
		}
    }	
       
    private void readPalette(InputStream is, int color_tb_bytes) throws Exception
    {
		int index = 0, nindex = 0;
		byte buf[] = new byte[color_tb_bytes];
		is.read(buf,0,color_tb_bytes);

		for(int i = 0; i < color_tb_bytes; i += 3)
		{
			colorPalette[index++] = ((0xff<<24)|((buf[nindex++]&0xff)<<16)|((buf[nindex++]&0xff)<<8)|(buf[nindex++]&0xff));
		}
    }
   
    private void unpackTrueColorPcxFile(InputStream is) throws Exception
    {
       int index = 0,nindex = 0;
       int index1 = 0,index2 = 0;
	   int bt,bt1 = 0,num_of_rep = 0;
       int totalBytes = 0;
	   int skip = 0, offset1, offset2;
       int scanline[];

	   indexedColor = false;

       int available = is.available();

	   byte brgb[] = new byte[available];

      /**
	   * A BufferedInputStream could have been constructed from the InputStream,
	   * but for maximum decoding sppeed, one time reading of the image data 
	   * into memory is ideal though this is memory consuming.
	   */
	   int buf_len = is.read(brgb,0,available);
	 
	   System.out.println("true color pcx image!");
		
	   totalBytes = NPlanes * bytesPerLine;
	   scanline = new int[totalBytes];
       skip = bytesPerLine-width;

 label:

	   for(int i = 0; i < height && nindex < buf_len; i++)
	   {
			   index = 0;
			   index2 = 0;
			   offset1 = width+skip;
	           offset2 = 2*offset1;

			   
			   do{
   			        bt = (brgb[nindex++]&0xff);
				    if((bt&0xC0) == 0xC0)
				    {
				        num_of_rep = bt&0x3F;
                        bt1 = (brgb[nindex++]&0xff);
				        for(int k = 0; (k<num_of_rep)&&(index<totalBytes); k++)
				        {
							 scanline[index++] = bt1; 
			    		}
						if (nindex >= buf_len)
						{
							//break label;
							break;
						}
				    }
				    else  
				    {
					    scanline[index++] = bt;
						if (nindex >= buf_len)
					    {
						    //break label;
						    break;
					    }
			        }
				 }while(index<totalBytes);
				 
			    for(int n = 0; n < width; n++)
				{
			      pix[index1++] = ((255<<24)|(scanline[index2++]<<16)|(scanline[offset1++]<<8)|(scanline[offset2++]));
			    }
	   }
	   is.close();
	}
   	
	private void unpack256ColorPcxFile(InputStream is) throws Exception
	{
        int scanLine[];
        int index = 0,nindex = 0;
        int index1 = 0;
	    int bt = 0,totalBytes = 0;
		int num_of_rep = 0;
        int bt1 = 0;
	    
		totalBytes = bytesPerLine*NPlanes;
		
        scanLine = new int[totalBytes];

		int available = is.available();

		int colorsUsed = (1<<NPlanes*pcxHeader.bits_per_pixel);
		int color_tb_bytes = 3*colorsUsed;
		
		colorPalette = new int[colorsUsed];

		int buf_len = available-color_tb_bytes;
		byte brgb[] = new byte[buf_len];
        buf_len = is.read(brgb,0,buf_len);

        readPalette(is,color_tb_bytes);

	   /** 
	    * If a BufferedInputStream is used for a 256-color image, we have to use 
		* mark(int), skip(long) and reset() methods to put the stream pointer to
		* the beginning of the color palette, fill the color palette and reset the pointer to
		* the beginning of the image data. This is awkward and equally memory consuming since
		* the system must maintain a buffer for the reset() method and we have to keep track
		* of the skip(long) method to ensure reading of the required bytes as shown below:
		* 	
		* <p> 
		* is.mark(available);
        * long i = 0, count = 0;
		*
        * while(count<buf_len)
		*  {
		*     i = is.skip(buf_len-count);
        *     count += i;
		*  }
		*
		* readPalette(is,color_tb_bytes);
		* is.reset();
        *
		* @see        java.io.BufferedInputStream
		*/

		System.out.println("256 color pcx image!");

 label:

		for(int i = 0; i < height && nindex < buf_len; i++)
		{			  
		         index = 0;
				
				 do{
				    bt = brgb[nindex++]&0xff;
					if((bt&0xC0) == 0xC0)
				    {
				        num_of_rep = bt&0x3F;
                        bt1 = brgb[nindex++]&0xff;

						//used to deal with the situation when the image just
						//dosen't hold enough data for the specified image width
						//the sample image gmarbles.pcx is an example... 
					
				        for(int k = 0; (k<num_of_rep)&&(index<totalBytes); k++)
				        {
							scanLine[index++] = bt1;
			            }
  					    
						if (nindex >= buf_len)
						{
							//break label;
							break;
						}
				    }
				    else  
				    {
					    scanLine[index++] = bt;

						if (nindex >= buf_len)
					    {
						    //break label;
						    break;
					    }
				    }
				    
				 }while(index<totalBytes);
		        
			     for(int n = 0; n < width; n++)
				 {
			        pix[index1++] = colorPalette[scanLine[n]];
		         }
		}
		is.close();
	}

	private void unpackOneBitEgaPcxFile(InputStream is) throws Exception
	{
	    //decoding pcx images with 1 bits_per_pixel per color plane 
		//using 1-4 color planes

		int scanLine[][] = new int[NPlanes][width/8+1];
        int index = 0, nindex = 0;
        int index1 = 0, index2 = 0, index3 = 0;
	    int bt = 0, totalBytes = 0;
		int num_of_rep = 0;
        int bt1 = 0, buf[], skip = 0;
	    
        int available = is.available();

		byte brgb[] = new byte[available];

	    int buf_len = is.read(brgb,0,available);

		totalBytes = bytesPerLine*NPlanes;
        buf = new int[totalBytes];

        int bits_per_width = pcxHeader.bits_per_pixel*width;

		if (bits_per_width%8 == 0)
		{
			skip = bytesPerLine-bits_per_width/8;
		}
		else skip = bytesPerLine-bits_per_width/8-1;

 label:

		for(int i = 0; i < height && nindex < buf_len; i++)
		{			  
		         index = 0;
				 index2 = 0;
				 index3 = 0;
						 
				 do{
				    bt = brgb[nindex++]&0xff;
				    if((bt&0xC0) == 0xC0)
				    {
				        num_of_rep = bt&0x3F;
                        bt1 = brgb[nindex++]&0xff;
				  
				        for(int k = 0; (k<num_of_rep)&&(index<totalBytes); k++)
				        {
							buf[index++] = bt1;
				        }
						if (nindex >= buf_len)
						{
							//break label;
							break;
						}
				    }
				    else  
				    {
					    buf[index++] = bt;

						if (nindex >= buf_len)
					    {
						   //break label;
						   break;
					    }
				    }
				 }while(index<totalBytes);
			
				 for (int j = 0; j < NPlanes; j++)
				 {
					for (int k = 0; k < width/8; k++)
					{
						scanLine[j][k] = buf[index2++];
					}
					if (width%8 != 0)
					{
						scanLine[j][width/8] = buf[index2++];
					}
					index2 += skip;
				 }

		         switch (NPlanes)
		         {
		           case 4:
				   {
					  for(int n = 0; n < width/8; n++)
				      {
					    for (int l = 1; l < 9; l++)
					    {
                          index3 = (((scanLine[3][n]>>>(8-l))&0x01)<<3)|(((scanLine[2][n]>>>(8-l))&0x01)<<2)
							  |(((scanLine[1][n]>>>(8-l))&0x01)<<1)|((scanLine[0][n]>>>(8-l))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
		              }

				      if (width%8 != 0)
				      {
					    for (int m = 1; m < width%8+1; m++)
					    { 
                          index3 = (((scanLine[3][width/8]>>>(8-m))&0x01)<<3)|(((scanLine[2][width/8]>>>(8-m))&0x01)<<2)
							  |(((scanLine[1][width/8]>>>(8-m))&0x01)<<1)|((scanLine[0][width/8]>>>(8-m))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
				      }
					  break;
				   }
		           case 3:
				   {
                      for(int n = 0; n < width/8; n++)
				      {
					    for (int l = 1; l < 9; l++)
					    {
                          index3 = (((scanLine[2][n]>>>(8-l))&0x01)<<2)|(((scanLine[1][n]>>>(8-l))&0x01)<<1)
							  |((scanLine[0][n]>>>(8-l))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
		              }

				      if (width%8 != 0)
				      {
					    for (int m = 1;m<width%8+1;m++)
					    { 
                          index3 = (((scanLine[2][width/8]>>>(8-m))&0x01)<<2)|(((scanLine[1][width/8]>>>(8-m))&0x01)<<1)
							  |((scanLine[0][width/8]>>>(8-m))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
				      }
					  break;
				   }
				   case 2:
				   {
                      for(int n = 0; n < width/8; n++)
				      {
					    for (int l = 1; l < 9; l++)
					    {
                          index3 = (((scanLine[1][n]>>>(8-l))&0x01)<<1)|((scanLine[0][n]>>>(8-l))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
		              }

				      if (width%8 != 0)
				      {
					    for (int m = 1; m < width%8+1; m++)
					    { 
                          index3 = (((scanLine[1][width/8]>>>(8-m))&0x01)<<1)|((scanLine[0][width/8]>>>(8-m))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
				      }
					  break;
				   }
				   case 1:
				   {
					  int BW_palette[] = new int[2];
                      
		              BW_palette[0] = 0xff000000;
		              BW_palette[1] =0xff000000|0xff0000|0xff00|0xff;

					  colorPalette = BW_palette;

					  for(int n = 0; n < width/8; n++)
				      {
					    for (int l = 1; l < 9; l++)
					    {
                          index3 = ((scanLine[0][n]>>>(8-l))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
		              }

				      if (width%8 != 0)
				      {
					    for (int m = 1; m < width%8+1; m++)
					    { 
                          index3 = ((scanLine[0][width/8]>>>(8-m))&0x01);
						  pix[index1++] = colorPalette[index3];
					    }
				      }
					  break;
				   }
		           default:
					  //do something
		         }
			     
		}
		is.close();
	}

	private void unpackOnePlaneEgaPcxFile(InputStream is) throws Exception
	{
		//trys to decode 4 and 16 color images as implemented 
		//by using 2 and 4 bits per pixel and one color plane

        int index = 0,nindex = 0;
        int index1 = 0,index2 = 0;
	    int bt = 0,totalBytes = 0;
		int num_of_rep = 0;
        int bt1 = 0,buf[];

        int available = is.available();

		byte brgb[] = new byte[available];

	    int buf_len = is.read(brgb,0,available);

		totalBytes = bytesPerLine;

        buf = new int[totalBytes];

 label:

		for(int i = 0; i < height && nindex < buf_len; i++)
		{			  
		         index = 0;
				 index2 = 0;
						 
				 do{
				    bt  =  brgb[nindex++]&0xff;

				    if((bt&0xC0) == 0xC0)
				    {
				        num_of_rep = bt&0x3F;
                        bt1 = brgb[nindex++]&0xff;
				  
				        for(int k = 0; (k<num_of_rep)&&(index<totalBytes); k++)
				        {
							buf[index++] = bt1;
				        }

						if (nindex >= buf_len)
						{
							//break label;
							break;
						}
				    }
				    else  
				    {
					    buf[index++] = bt;

						if (nindex >= buf_len)
					    {
						   //break label;
						   break;
					    }
				    }
				 }while(index<totalBytes);
                 
				 switch (bitsPerPixel)
                 {
                    case 4:
						 for (int k = 0; k < width/2; k++)
				         {
					        pix[index1++] = colorPalette[((buf[index2]>>4)&0x0F)];
					        pix[index1++] = colorPalette[(buf[index2++]&0x0F)];
				         }
						 if ((width%2) != 0)
						 {
							 pix[index++] = colorPalette[((buf[index2]>>4)&0x0F)];
						 }
						 break;
					case 2:
                         for (int k = 0; k < width/4; k++)
				         {
					        pix[index1++] = colorPalette[((buf[index2]>>6)&0x03)];
                            pix[index1++] = colorPalette[((buf[index2]>>4)&0x03)];
                            pix[index1++] = colorPalette[((buf[index2]>>2)&0x03)];
					        pix[index1++] = colorPalette[(buf[index2++]&0x03)];
				         }
						 if ((width%4) != 0)
						 {
							 switch (width%4)
							 {
							    case 1: pix[index1++] = colorPalette[((buf[index2]>>6)&0x03)];
								        break;
								case 2: pix[index1++] = colorPalette[((buf[index2]>>6)&0x03)];
								        pix[index1++] = colorPalette[((buf[index2]>>4)&0x03)];  
										break;
								case 3: pix[index1++] = colorPalette[((buf[index2]>>6)&0x03)];
                                        pix[index1++] = colorPalette[((buf[index2]>>4)&0x03)];
                                        pix[index1++] = colorPalette[((buf[index2]>>2)&0x03)];
							            break;
							 }
						 }
					default:  //do something
                 }
		}
		is.close();
	}

    /**
	public static void main(String args[]) throws Exception
	{

		FileInputStream fs = new FileInputStream(args[0]);
        PCXReader reader = new PCXReader();

		long t1 = System.currentTimeMillis();
		reader.unpackImage(fs);
		long t2 = System.currentTimeMillis();
		
		System.out.println("Decoding time: "+(t2-t1)+"ms");
		
		fs.close();
		
		int pix[] = reader.getImageData();
		Dimension dm = reader.getImageSize();
		int width = dm.width;
		int height = dm.height;

		final JFrame jframe = new JFrame("PCXReader");

		jframe.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent evt)
			{
				jframe.dispose();
				System.exit(0);
			}
		});
		Image img = jframe.createImage(new MemoryImageSource(width, height, pix, 0, width));
        JLabel theLabel = new JLabel(new ImageIcon(img));
        jframe.getContentPane().add(new JScrollPane(theLabel));
        jframe.setSize(400,400);
        jframe.setVisible(true);
	}
     */
    private class PcxHeader
    {
		byte  manufacturer;
		byte  version;
/*************************************************************************************************
	Value    PC Paintbrush Version                            Description
      0         Version 2.5                          with fixed EGA palette information
	  2         Version 2.8                          with modifiable EGA palette information
	  3         Version 2.8                          without (usefull) palette information
	                                                 (a decoder should provide its own palette)
      4         PC Paintbrush for windows            *******************************************
	  5         Version 3.0 of PC Paintbrush         * Possible VGA 256 color palette attached *
	            PC Paintbrush Plus                   * and all 24-bit image files              *
				PC Paintbrush Plus for Windows       *                                         *
				Publisher's Paintbrush               
 *///**********************************************************************************************
		byte  encoding;
		byte  bits_per_pixel;
		short xmin,ymin;
		short xmax,ymax;
		short hres;
		short vres;
		int  colorPalette[] = new int[16];
        byte  reserved;
		byte  color_plane;
		short bytes_per_line;
		short palette_type;
		byte  filler[] = new byte[58];

	    void readHeader(InputStream is) throws Exception
	    {
		   int nindex = 0;
	       byte buf[] = new byte[128];

	       is.read(buf,0,128);
		
		   manufacturer = buf[nindex++];
		   version = buf[nindex++];
		   encoding = buf[nindex++];
		   bits_per_pixel = buf[nindex++];
		
		   xmin = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   ymin = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   xmax = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   ymax = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   hres = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   vres = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
  
		   for(int i = 0; i < 16; i++)
		   {
		      colorPalette[i] = ((0xff<<24)|((buf[nindex++]&0xff)<<16)|((buf[nindex++]&0xff)<<8)|(buf[nindex++]&0xff));
		   }
    	
		   reserved = buf[nindex++];
		   color_plane = buf[nindex++];
		
		   bytes_per_line = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));
		   palette_type = (short)((buf[nindex++]&0xff)|((buf[nindex++]&0xff)<<8));

		   for(int i = 0; i < 58; i++)
		   {
		      filler[i] = buf[nindex++];
		   }
	    }
   }
}
