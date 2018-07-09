--TEST--
helloOrNotHello(true), helloOrNotHello(false)
--FILE--
<?php
helloOrNotHello(true);
helloOrNotHello(false);
helloOrNotHello(OK_HELLO);
--EXPECT--
Hello!
Nop!
Hello!
