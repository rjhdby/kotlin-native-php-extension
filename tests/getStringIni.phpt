--TEST--
echo getStringIni("example.count");
--FILE--
<?php
echo getStringIni("example.count");
--EXPECT--
10