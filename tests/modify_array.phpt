--TEST--
printArray(getArray(['a'=>'a','b'=>'b'], 'b', 'c'));
--FILE--
<?php
printArray(getArray(['a'=>'a','b'=>'b'], 'b', 'c'));
--EXPECT--
["a" => "a", "b" => "c"]