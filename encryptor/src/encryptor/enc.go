package main

import (
    "crypto/aes"
    "crypto/cipher"
    "crypto/rand"
    "errors"
    "fmt"
    "io"
    "log"
    "os"
    "strconv"
    "encoding/hex"
)

func main() {
    key := []byte("HelloThisIsEncryptionKey01234567")

    text := os.Args[1]
    toEncrypt := os.Args[2]
    b, _ := strconv.ParseBool(toEncrypt)
    // encrypt text.
    if b == true {
        textBytes := []byte(text)
        encryptedBytes, err := encrypt(key, textBytes)
        if err != nil {
            log.Fatal(err)
        }
        dst := make([]byte, hex.EncodedLen(len(encryptedBytes)))
        hex.Encode(dst, encryptedBytes)
        fmt.Printf("%s", dst)
    } else {
        textBytes := []byte(text)
        dst := make([]byte, hex.DecodedLen(len(textBytes)))
        hex.Decode(dst, textBytes)
        decryptedBytes, err := decrypt(key, dst)
        if err != nil {
            log.Fatal(err)
        }
        fmt.Printf("%s", decryptedBytes)
    }
}

func encrypt(key, text []byte) ([]byte, error) {
    block, err := aes.NewCipher(key)
    if err != nil {
        return nil, err
    }
    ciphertext := make([]byte, aes.BlockSize + len(text))
    iv := ciphertext[:aes.BlockSize]
    if _, err := io.ReadFull(rand.Reader, iv); err != nil {
        return nil, err
    }
    cfb := cipher.NewCFBEncrypter(block, iv)
    cfb.XORKeyStream(ciphertext[aes.BlockSize:], text)
    return ciphertext, nil
}

func decrypt(key, text []byte) ([]byte, error) {
    block, err := aes.NewCipher(key)
    if err != nil {
        return nil, err
    }
    if len(text) < aes.BlockSize {
        return nil, errors.New("ciphertext too short")
    }
    iv := text[:aes.BlockSize]
    text = text[aes.BlockSize:]
    cfb := cipher.NewCFBDecrypter(block, iv)
    cfb.XORKeyStream(text, text)
    return text, nil
}