--TEST--
hello($name) function test
--FILE--
<?php
echo hello('JoE');
--EXPECT--
Hello JoE!!!
