--TEST--
printMixed(["Test",10,23.14,true,false,['b'=>'a',11,'a'=>'b','c',8=> 2.4,7=>'asddsa',['a','b']],null, new DateTime()]);
--FILE--
<?php
printMixed(["Test",10,23.14,true,false,['b'=>'a',11,'a'=>'b','c',8=> 2.4,7=>'asddsa',['a','b']],null, new DateTime()]);
--EXPECT--
Test
10
23.14
true
false
["b" => "a", 0 => 11, "a" => "b", 1 => "c", 8 => 2.4, 7 => "asddsa", 9 => [0 => "a", 1 => "b"]]
null
Mixed
