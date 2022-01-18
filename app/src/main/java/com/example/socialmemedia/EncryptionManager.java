//generate two (large) primes - private p and q
//        public modulus r=pq
//        private totient phi= (p-1)(q-1)
//        comprime
//
//        public key e, 1<e<phi,
//  e coprime with phi to get unique? d value
//          private key d,inverse of e mod phi,
//        (e*d) mod phi = 1

//      Euclidean algorithm
//      dividend= quotient * divisor + remainder
//      a=qb+r
//      b=qr+r2
//      r=qr2+r3
//
//      dividend = bigNum
//      divisor = smallNum
//
//        Encryption: c=m^e mod r
//        m<r , plaintext binary encrypted in blocks, value of <r
//  convert to denary first for binary value, to apply RSA fewer times
//          Decryption: m=c^d mod r

package com.example.socialmemedia;

import android.content.Intent;
import android.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class EncryptionManager {

    public static void main(String[] args) {
        EncryptionManager encryptionManager = new EncryptionManager();
//        for (int i=1;i<10;i++){
//            System.out.println("E: "+encryptionManager.RSAencrypt(String.valueOf(i), encryptionManager.publicKey, encryptionManager.getPublicModulus()));
//        }

//        String encrypt = encryptionManager.RSAencrypt("9", encryptionManager.publicKey, encryptionManager.publicModulus);
//        System.out.println("E: "+encrypt);
//
//        System.out.println("D: "+encryptionManager.RSAdecrypt(encrypt, encryptionManager.privateKey, encryptionManager.publicModulus));
//
//        String encrypt2 = encryptionManager.RSAencrypt("9sdfsdf", 92099, 107221);
//        System.out.println("E: "+encrypt2);
//
//        System.out.println("D: "+encryptionManager.RSAdecrypt(encrypt2, 26483, 107221));

//        String encryptedMessage = encryptionManager.RSAencrypt("ooh shaka laka oof 哈哈 🆒",410589,948667);
//        System.out.println("Encrypted "+encryptedMessage);
//        System.out.println("Decrypted "+encryptionManager.RSAdecrypt(encryptedMessage,171989,948667));
//
//        String symEncryptMessage = encryptionManager.symmetricEncrypt("a oof 哈哈 🆒",4);
//        System.out.println("Encrypted: "+symEncryptMessage);
        String symEncryptMessage = "66536903609360936023953619360536063618395360636183953618369636003610369036093013617369036903699317315365536183619361036993690360136193618301369736053696361836113606369830136963698301361036083";
        System.out.println("Decrypted: "+ encryptionManager.symmetricDecrypt(symEncryptMessage,5));

//        System.out.println("unicode "+Integer.toString('d'));

//        String.valueOf('h').codePoints().mapToObj(Integer::toOctalString).forEach(System.out::println);
//        EncryptionManager encryptionManager = new EncryptionManager();

//        System.out.println("r: "+encryptionManager.getPublicModulus());
//        System.out.println("e: "+encryptionManager.getPublicKey());
//        System.out.println("d: "+encryptionManager.getPrivateKey());
//        System.out.println("gcd: "+encryptionManager.getGCD(encryptionManager.publicKey, encryptionManager.privateKey));
    }  //for testing

    private ArrayList<Integer> primeList,coprimeList;
    private int[] markedCompositeList;
    private static final int MAX=1000;     //maximum value of the primes
    private int prime1, prime2, publicModulus, totient, publicKey, privateKey;

    public EncryptionManager() {
        markedCompositeList= new int[MAX];
        for(int i=0;i<MAX;i++){
            markedCompositeList[i]=0;
        }
        primeList=new ArrayList<>();
        setPrimeList();

        prime1 = primeList.get(getRandInt(1,primeList.size()-1));    //gets random prime number
        do{
            prime2 = primeList.get(getRandInt(1,primeList.size()-1));   //regenerate q so that p!=q

        }while(prime1==prime2);


        publicModulus = prime1*prime2;
        totient = (prime1-1)*(prime2-1);

        coprimeList = new ArrayList<>();
        coprimeList = getCoprimeList(totient,getPrimeFactors(totient));


        int count=0;
        do {             //repeats to get different public and private keys i.e e!=d, but stop if can't find one
            publicKey = coprimeList.get(getRandInt(2, coprimeList.size()) - 1);   //e is a coprime of the totient, so gcd(e,phi)=1
            //e>1 because 1 is coprime with everything

            privateKey = getLinearCombinationCoefficients(totient, publicKey)[1];
            if (privateKey < 0) {
                privateKey += totient;   //equivalent positive value (mod phi)
            }
            count++;
            /*d=coefficients[1] because d is inverse of e mod phi: e*d=1 (mod phi)
              and when you take the mod phi of xphi+ye=1, you are left with ye,
              e<phi so d=y the smaller number coefficient at index 1
             */
        }while (privateKey==publicKey &&count<10);       //count less than 10 to stop infinite loop
    }


    public int getRandInt(int lowerBound, int upperBound) {    //both inclusive

        int range= upperBound - lowerBound;
        double doubleRandom = Math.random();    //random decimal [0,1)
        double randomInRange = (doubleRandom * range)+lowerBound;
        int randomInt = (int)Math.round(randomInRange);        //rounding allows both bounds to be included

        return randomInt;
    }

    public int getPrivateKey(){
        return privateKey;
    }

    public int getPublicKey(){
        return publicKey;
    }

    public int getPublicModulus(){
        return publicModulus;
    }

    private void setPrimeList(){                   //lists prime numbers up to max
        int stopCheckingPoint = (int)Math.ceil(Math.sqrt(MAX));
        //Sieve of Eratosthenes
        for (int number=2;number<stopCheckingPoint;number++){
            if(markedCompositeList[number]==0){   //its prime still
                primeList.add(number);
                int largestMultiple = (int) Math.ceil(MAX/number);
                for (int multiplier=2;multiplier<largestMultiple;multiplier++){
                    markedCompositeList[number*multiplier]=1;  // a multiple of a prime is composite
                }
            }
        }
        for(int index=stopCheckingPoint;index<MAX;index++){    //remember to add all primes after the stop point
            if(markedCompositeList[index]==0){
                primeList.add(index);
            }
        }
//        Log.d(TAG, "setPrimeList: "+primeList);
    }

    private ArrayList<Integer> getPrimeFactors(int number){
        ArrayList<Integer> primeFactors = new ArrayList<>();
        for(Integer prime: primeList){
            if(prime<=number && number%prime==0){         //is a prime factor
                primeFactors.add(prime);
                number = number / prime;
            }
        }
        return primeFactors;
    }

    private ArrayList<Integer> getCoprimeList(int number, ArrayList<Integer> primeFactorList){
        int[] markedCoprime = new int[number];               //1 if coprime, 0 if not
        ArrayList<Integer> coprimeList = new ArrayList<>();

        for (int index=1;index<number;index++){         //coprime has to be less than number itself
            markedCoprime[index]=1;   // initialise all to coprime, then discard  if not coprime
        }

        for (Integer primeFactor: primeFactorList){
            for (int multiplier=1;multiplier<(int)number/primeFactor;multiplier++){
                markedCoprime[multiplier*primeFactor]=0;     //multiples of prime factors are not coprime
            }
        }

        for (Integer index=1;index<number;index++){
            if(markedCoprime[index]==1){    //value of index is coprime
                coprimeList.add(index);
            }
        }

        return coprimeList;

    }

    /*Bezout's identity/ extended euclidean algorithm methods
    >given gcd, rewrite as linear recombination in form ax+by=gcb(a,b) to find x and y*/

    //rewrite gcd as linear combination of two inputs
    //dividend,divisor are a,b respectively
    //ax+by = gcd(a,b)
    private int[] getLinearCombinationCoefficients(int dividend, int divisor){    //returns x and y
        int remainder = dividend % divisor;      //calculates new remainder and quotient in each call
        int quotient = dividend/divisor;

        if(divisor%remainder ==0){     //base case: next remainder is zero,
            //int gcd = remainder;       //means this remainder is the gcd
            //gcd= dividend - quotient*divisor
            // bigNum= dividend;
            // smallNum = divisor;
            int bigNumCoefficient = 1;
            int smallNumCoefficient = -quotient;

            int[] coefficients = {bigNumCoefficient,smallNumCoefficient};
            return coefficients;

        }else{         //recurse
            int[] coefficientsPrevious = getLinearCombinationCoefficients(divisor,remainder);
            //euclidean algorithm, divisor becomes new dividend, remainder becomes new divisor

            //rewrite smaller num r=a-qb
            //small num coefficient becomes new bigger num coefficient
            int bigNumCoefficient = coefficientsPrevious[1];
            //calculate new smaller num coefficient
            // = previous small num coefficient * -quotient + previous big num coefficient
            int smallNumCoefficient = coefficientsPrevious[1]*-quotient + coefficientsPrevious[0];

            int[] newCoefficients = {bigNumCoefficient,smallNumCoefficient};
            return newCoefficients;
        }
    }


    /**RSA encryption**/
    public String RSAencrypt(String message, int givenPublicKey, int publicModulusR){
        /**prepare the message**/
        /*convert each letter in message convert char to unicode (denary)
          convert denary unicode to octal(0-7)
          add "8" as suffix to indicate end of a character
         */
        String octalPlaintextUnicode="";
        for(int i=0;i<message.length();i++){
            String octalCharUnicode = Integer.toOctalString(message.charAt(i))+"8";
            //automatically converts to octal unicode
            octalPlaintextUnicode+=octalCharUnicode;
        }
        System.out.println("octal "+octalPlaintextUnicode);

        /*split into blocks of equal length
          add "9" as padding to the last block
         */
        ArrayList<String> plaintextBlocksList = new ArrayList<>();   //2D array containing blocks of text, separated into equal sizes
        int blockLength = String.valueOf(publicModulusR).length()-1; // to ensure each message block value < modulus value
        System.out.println("blocklength "+blockLength);
        int startPointer =0;  //index of where a block starts
        while (startPointer<octalPlaintextUnicode.length()){
            String block;
            if(startPointer+blockLength<octalPlaintextUnicode.length()) {   //if there are still more full blocks
                block = octalPlaintextUnicode.substring(startPointer, startPointer + blockLength);   //creates blocks of plaintext
            }else {
                block = octalPlaintextUnicode.substring(startPointer);   //if final block is too short just add what is left
                //but also pad block with 9's at end so that it is the block length
                block += new String(new char[blockLength-block.length()]).replace("\0","9");
                //creates empty string, then replace empty with 9's
            }
            startPointer+=blockLength;
            plaintextBlocksList.add(block);
        }
        System.out.println(plaintextBlocksList);

        /**encryption**/
        /*c=m^e mod r   encrypt in denary
          convert to octal 0-7
          pad with 9 at the end of each block - different to the 9 used for the last block padding, as the encrypted block is turned to octal again
         */
        String octalEncryptedText ="";
        for (String messageBlock: plaintextBlocksList){
            BigInteger messageBlockBigInt = new BigInteger(messageBlock);
            BigInteger publicKeyBigInt = new BigInteger(String.valueOf(givenPublicKey));
            BigInteger publicModulusBigInt = new BigInteger(String.valueOf(publicModulusR));

            BigInteger denaryValueEncryptedBlockBigInteger = messageBlockBigInt.modPow(publicKeyBigInt,publicModulusBigInt);
            System.out.println("denary encrypt block: "+ denaryValueEncryptedBlockBigInteger.toString());
            octalEncryptedText+= Integer.toOctalString(Integer.parseInt(denaryValueEncryptedBlockBigInteger.toString()))+"9";
        }
        return octalEncryptedText;
    }

    //DECRYPTION
    public String RSAdecrypt(String cipherMessage, int givenPrivateKey, int publicModulusR){
    /*1)remove 9s to obtain octal blocks
    2)convert from octal blocks to denary blocks
    3)**RSA decrypt to get denary plaintext - each block pad with 0's and get rid of 9's at end
    4)split using 8's to get separate octal unicode characters
    5)convert octal to denary unicode
    6)convert to symbols*/
        ArrayList<String> octalCipherBlockList = new ArrayList<>( Arrays.asList(cipherMessage.split("9")));

        String octalDecryptedText="";
        int blockLength = String.valueOf(publicModulusR).length()-1; //ensure decrypted blocks are the original block length

        for (String octalBlock: octalCipherBlockList){
            int denaryCipherBlock = Integer.parseInt(octalBlock,8);  //converts octal to denary
            BigInteger denaryCipherBlockBigInt = new BigInteger(String.valueOf(denaryCipherBlock));
            BigInteger privateKeyBigInt = new BigInteger(String.valueOf(givenPrivateKey));
            BigInteger publicModulusBigInt = new BigInteger(String.valueOf(publicModulusR));

            //Decryption: m=c^d mod r
            BigInteger octalValueDecryptedBlockBigInt = denaryCipherBlockBigInt.modPow(privateKeyBigInt,publicModulusBigInt);
            String octalValueDecryptedBlock = String.valueOf(octalValueDecryptedBlockBigInt);
            System.out.println("before: "+denaryCipherBlockBigInt+"after: "+octalValueDecryptedBlockBigInt);

            System.out.println("blocklen"+blockLength+"actual len"+octalValueDecryptedBlock.length());
            System.out.println("acutal bloc"+octalValueDecryptedBlock);
            String padding = new String(new char[blockLength-String.valueOf(octalValueDecryptedBlock).length()]).replace("\0","0");
            //if the decrypted block is not the defined block length as it should be,
            //then some 0's may have been omitted at the start, which are lost due to mathematical redundancy
            //the last block was filled with 9's to make it up to block length, so should still work out missing 0's if any
            octalValueDecryptedBlock = padding + octalValueDecryptedBlock;

            if(octalCipherBlockList.indexOf(octalBlock)==octalCipherBlockList.size()-1){   //for the last block
            //remove empty space placeholder 9's for the block, which may be shorter than block length
                octalValueDecryptedBlock= octalValueDecryptedBlock.replace("9","");
            }

            octalDecryptedText+=octalValueDecryptedBlock;
        }

        ArrayList<String> octalCharacterUnicodeList = new ArrayList<>(Arrays.asList(octalDecryptedText.split("8")));
        String decryptedTextMessage = "";

        for(String characterUnicode: octalCharacterUnicodeList){       //for every character
            int denaryUnicode = Integer.parseInt(characterUnicode,8);   //convert octal to denary
            decryptedTextMessage += (char) denaryUnicode;    //convert unicode to character
        }
        return decryptedTextMessage;
    }

    public String symmetricEncrypt(String message, int symmetricKey){
        String encryptedMessage = "";
        String octalMessageUnicode="";

        for (int charIndex=0;charIndex<message.length();charIndex++) {   //for every letter in message
            String charUnicode = Integer.toOctalString(message.charAt(charIndex));  //convert to unicode, then convert to octal (0-7)
            // append an '8' at the end to indicate each character, then add to string
            octalMessageUnicode +=charUnicode + "8";
        }
        for (int index=0;index<octalMessageUnicode.length();index++){      //for every number in unicode string
            int digitCipher = Character.getNumericValue(octalMessageUnicode.charAt(index)) + symmetricKey;      //shift number by key

            if (digitCipher>9){
                digitCipher -= 10;                         //mod 10 to keep it a single digit
            }
            encryptedMessage+=String.valueOf(digitCipher);
        }
        return encryptedMessage;    //a string of numbers with digits shifted by the key

    }

    public String symmetricDecrypt(String cipherText, int symmetricKey){
        String decryptedUnicode="";

        for (int index=0;index<cipherText.length();index++){               //for every number in the ciphertext
            int digitPlain = Character.getNumericValue(cipherText.charAt(index))-symmetricKey;     //shift the number back by the key

            if (digitPlain<0){                 //-1 loops back to 9 to keep it a single digit 0-9
                digitPlain += 10;
            }
            decryptedUnicode+=String.valueOf(digitPlain);
        }
        String[] charactersUnicode = decryptedUnicode.split("8");
        //strings of character unicode were indicated by '8' at end of an octal number
        String decryptedMessage ="";

        for(String charUnicode: charactersUnicode){    //for every string of numbers, each represents a character
            decryptedMessage += (char)Integer.parseInt(charUnicode,8);
            //convert from octal to denary unicode then to corresponding character
        }

        return decryptedMessage;

    }
}
