--TEST--
hello('JoE', HELLO_ES)
--FILE--
<?php
echo hello('JoE', HELLO_ES);
--EXPECT--
Hola JoE!!!
