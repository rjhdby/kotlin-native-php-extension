--TEST--
echo iniValueFor("example.count").iniValueFor("example.name");
--FILE--
<?php
echo iniValueFor("example.count").iniValueFor("example.name");
--EXPECT--
10example