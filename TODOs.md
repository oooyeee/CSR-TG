# TODOs
* change server bufferring  
> NOW: server cannot track and store messages bigger than read buffer  
> TODO: store messages in chunks (use frame length logic)
* improve DH + AES
> NOW: imperfect AES key and iv derivation
> TODO: add DKHF, or maybe count frames and change iv from dh.result hash