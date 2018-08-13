--TEST--
Test printMixed. String, Long, Double, true, false, array
--FILE--
<?php
printMixed("Test");
printMixed(10);
printMixed(23.14);
printMixed(true);
printMixed(false);
printMixed([0,1,2]);
--EXPECT--
Test
10
23.14
true
false
Mixed