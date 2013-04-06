mixnet
======

A mixnet is a tool to provide anonymity for a group of senders by use of proxy servers. Each proxy server supplies a public key which a sender encrypts one layer on top of its a message with.
This means that the message should not be traceable back to the sender, but instead to the group of senders.

This is a mixnet with abort and error tracing based on a more formal protocol. This means that it should be able to detect if
a mixnode or sender is misbehaving, and abort before each message is revealed.

* Lol, what testing?
* Does not support error tracing at the moment, but should abort depending on different circumstances.

Comes with a sample application for voting.
