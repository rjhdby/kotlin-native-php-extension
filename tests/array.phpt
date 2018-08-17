--TEST--
printArray(['b'=>'a',11,'a'=>'b','c',8=> 2.4,7=>'asddsa',['a','b']]);
--FILE--
<?php
printArray(['b'=>'a',11,'a'=>'b','c',8=> 2.4,7=>'asddsa',['a','b']]);
--EXPECT--
["b" => "a",0 => 11,"a" => "b",1 => "c",8 => 2.4,7 => "asddsa",9 => [0 => "a",1 => "b"]]