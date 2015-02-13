#!/bin/bash
cd src/main/resources/app
npm install
p=$(npm bin)
$p/bower install
$p/gulp build