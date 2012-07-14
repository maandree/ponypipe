#!/bin/bash

#Hacked to make it work on ubuntu

## create directory for Java binaries
mkdir bin 2>/dev/null


## in with resources to bin/
if [ -d res ]; then
    cp -r res bin
fi


## java compiler if default is for Java 7
[[ $(javac -version 2>&1 | cut -d . -f 2) = '7' ]] &&
    function javacSeven()
    {   javac "$@"
    }

## java compiler if default is not for Java 7
[[ $(javac -version 2>&1 | cut -d . -f 2) = '7' ]] ||
    function javacSeven()
    {   javac "$@"
    }


## java executer if default is for Java 7
[[ $(echo `java -version 2>&1 | cut -d . -f 2` | cut -d ' ' -f 1) = '7' ]] &&
    function javaSeven()
    {   java "$@"
    }

## java executer if default is not for Java 7
[[ $(echo `java -version 2>&1 | cut -d . -f 2` | cut -d ' ' -f 1) = '7' ]] ||
    function javaSeven()
    {   java "$@"
    }


## warnings
warns="-Xlint:all"

## standard parameters
params="-source 7 -target 7 -s src -d bin"


## libraries
jars=''
if [ -d lib ]; then
    jars=`echo $(find lib | grep .jar$) | sed -e 's/lib\//:lib\//g' -e 's/ //g'`
fi


## parse options
function _javac()
{   javac "$@"
}
paramEcho=0
paramEcj=0
paramJar=0
paramPkg=0
for opt in "$@"; do
    if [[ $opt = '-ecj' ]]; then
	paramEcj=1
	if [ -d /opt/java7/jre/lib ]; then
	    function _javac()
	    {   ecj -bootclasspath `echo $(find /opt/java7/jre/lib | grep .jar$) | sed -e 's/\/opt\/java7\/jre\/lib\//:\/opt\/java7\/jre\/lib\//g' -e 's/ //g' | dd skip=1 bs=1 2>/dev/null` "$@"
	    }
	else
	    function _javac()
	    {   ecj "$@"
	    }
	fi
    elif [[ $opt = '-echo' ]]; then
	paramEcho=1
	function _javac()
	{   echo "$@"
	}
    elif [[ $opt = '-jar' ]]; then
	paramJar=1
    elif [[ $opt = '-q' ]]; then
	warns=''
    elif [[ $opt = -pkg ]]; then
	paramPkg=1
    fi
done


## colouriser
function colourise()
{
    if [[ $paramEcho = 1 ]]; then
        cat
    elif [[ $paramEcj = 1 ]]; then
	if [[ -f "colourpipe.ecj.jar" ]]; then
            javaSeven -jar colourpipe.ecj.jar
	else
	    cat
	fi
    elif [[ -f "colourpipe.javac.jar" ]]; then
        javaSeven -jar colourpipe.javac.jar
    else
	cat
    fi
}


if [[ $paramJar = 1 ]]; then
    ## build jar
    cp -r bin/se .
    jar -cfm ponypipe.jar META-INF/MANIFEST.MF $(find se)
    rm -r se
else
    if [[ $paramPkg = 0 ]]; then
        ## completion
	. run.sh --completion--
    fi
    
    ## exception generation
    if [ -f 'src/se/kth/maandree/javagen/ExceptionGenerator.java' ]; then
        ## compile exception generator
	( javacSeven $warns -cp . $params 'src/se/kth/maandree/javagen/ExceptionGenerator.java'  2>&1
	) | colourise &&
	
        ## generate exceptions code
	javaSeven -ea -cp bin$jars "se.kth.maandree.javagen.ExceptionGenerator" -o bin -- $(find src | grep '.exceptions$')  2>&1  &&
	echo -e '\n\n\n'  &&
	
        ## generate exceptions binaries
	( javacSeven $warns -cp bin$jars -source 7 -target 7 $(find bin | grep '.java$')  2>&1
	) | colourise
    fi
    
    ## compile ponypipe
    ( _javac $warns -cp .:bin$jars -s src -d bin $(find src | grep '.java$')  2>&1
    ) | colourise
fi
