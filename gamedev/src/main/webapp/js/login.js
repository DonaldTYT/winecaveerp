document.getElementById("loginBtn").addEventListener("click", function() {
    loginWithWebAuthn();
});

async function loginWithWebAuthn() {
    try {
        // Step 1: Call backend to get the login challenge (public key credentials)
        const response = await fetch('/webauthn/login', { method: 'GET' });
        const challengeData = await response.json();

        // Step 2: Use the WebAuthn API to authenticate
        const publicKeyCredentialRequestOptions = {
            challenge: Uint8Array.from(atob(challengeData.challenge), c => c.charCodeAt(0)),
            rpId: challengeData.rpId,
            allowCredentials: challengeData.allowCredentials.map(cred => ({
                id: Uint8Array.from(atob(cred.id), c => c.charCodeAt(0)),
                type: 'public-key'
            })),
            timeout: 60000,
            userVerification: 'required' // Ensures Face ID or Touch ID is used
        };

        // Step 3: Request authentication from the browser (Face ID / Touch ID)
        const credential = await navigator.credentials.get({
            publicKey: publicKeyCredentialRequestOptions
        });

        // Step 4: Send the response back to the backend to verify the login
        const authenticationResponse = {
            id: credential.id,
            rawId: arrayBufferToBase64(credential.rawId),
            response: {
                authenticatorData: arrayBufferToBase64(credential.response.authenticatorData),
                clientDataJSON: arrayBufferToBase64(credential.response.clientDataJSON),
                signature: arrayBufferToBase64(credential.response.signature),
                userHandle: arrayBufferToBase64(credential.response.userHandle),
            },
            type: credential.type
        };

        // Send the authentication response to the server for verification
        const verifyResponse = await fetch('/webauthn/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(authenticationResponse)
        });

        const verifyData = await verifyResponse.json();

        if (verifyData.success) {
            document.getElementById("status").innerText = "Login successful!";
        } else {
            document.getElementById("status").innerText = "Authentication failed!";
        }

    } catch (error) {
        console.error("Error during WebAuthn authentication:", error);
        document.getElementById("status").innerText = "Authentication failed!";
    }
}

function arrayBufferToBase64(buffer) {
    return btoa(String.fromCharCode.apply(null, new Uint8Array(buffer)));
}