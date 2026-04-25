package com.servalabs.chat.libsignal.api.messages.multidevice;


import com.servalabs.chat.libsignal.api.messages.SignalServiceAttachment;

public class ContactsMessage {

  private final SignalServiceAttachment contacts;
  private final boolean                 complete;

  public ContactsMessage(SignalServiceAttachment contacts, boolean complete) {
    this.contacts = contacts;
    this.complete = complete;
  }

  public SignalServiceAttachment getContactsStream() {
    return contacts;
  }

  public boolean isComplete() {
    return complete;
  }
}
