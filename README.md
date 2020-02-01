# KPIService And Test

## The service
The service supports key operations using the Rivest, Shamir, Aldeman (RSA) algorithm. It implements the following interface through a Binder:

### KeyPair getMyKeyPair() - Generate and/or retrieve a user’s RSA KeyPair. 
The first call to this method will generate and store the keypair before returning it. Subsequent calls will return the
same key pair.
### void storePublicKey (String partnerName, String publicKey) – Store a key for a provided partner name

### RSAPublicKey getPublicKey(String partnerName) – Returns the public key associated with the provided partner name

### Additionally, it also implements a resetMyKeyPair() that will erase the respective stored keys.

## instrumented tests to do the following:
### Request a keypair

### Arbitrary text can be encrypted and decrypted using the returned keypair

### A stored keypair can be retrieved

### A stored key can be retrieved using partnerName
