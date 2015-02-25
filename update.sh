#!/bin/bash
cd src/main/resources/app
p=$(npm bin)
$p/bower install
$p/gulp build