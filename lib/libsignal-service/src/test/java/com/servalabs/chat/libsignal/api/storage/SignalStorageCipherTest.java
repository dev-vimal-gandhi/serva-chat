package com.servalabs.chat.libsignal.api.storage;

import org.junit.Test;
import com.servalabs.chat.core.models.storageservice.StorageItemKey;
import com.servalabs.chat.libsignal.protocol.InvalidKeyException;
import com.servalabs.chat.libsignal.internal.util.Util;

import static org.junit.Assert.assertArrayEquals;

public class SignalStorageCipherTest {

  @Test
  public void symmetry() throws InvalidKeyException {
    StorageItemKey key  = new StorageItemKey(Util.getSecretBytes(32));
    byte[]         data = Util.getSecretBytes(1337);

    byte[] ciphertext = SignalStorageCipher.encrypt(key, data);
    byte[] plaintext  = SignalStorageCipher.decrypt(key, ciphertext);

    assertArrayEquals(data, plaintext);
  }

  @Test(expected = InvalidKeyException.class)
  public void badKeyOnDecrypt() throws InvalidKeyException {
    StorageItemKey key  = new StorageItemKey(Util.getSecretBytes(32));
    byte[]         data = Util.getSecretBytes(1337);

    byte[] badKey = key.serialize().clone();
    badKey[0] += 1;

    byte[] ciphertext = SignalStorageCipher.encrypt(key, data);
    byte[] plaintext  = SignalStorageCipher.decrypt(new StorageItemKey(badKey), ciphertext);
  }
}
