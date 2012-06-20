#!/bin/sh

## completion
. run.sh --completion--


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
    {   javac7 "$@"
    }


## java executer if default is for Java 7
[[ $(echo `java -version 2>&1 | cut -d . -f 2` | cut -d ' ' -f 1) = '7' ]] &&
    function javaSeven()
    {   java "$@"
    }

## java executer if default is not for Java 7
[[ $(echo `java -version 2>&1 | cut -d . -f 2` | cut -d ' ' -f 1) = '7' ]] ||
    function javaSeven()
    {   java7 "$@"
    }


## warnings
warns="-Xlint:all"

## standard parameters
params="-source 7 -target 7 -s src -d bin"


## libraries
jars=''
if [ -d lib ]; then
    jars=`echo $(find lib | grep .jar$) | sed -e 's/lib\//:/g' -e 's/ //g'`
fi


## parse options
paramEcho=0
paramEcj=0
for opt in "$@"; do
    if [[ $opt = '-ecj' ]]; then
	paramEcj=1
	function javacSeven()
	{   ecj "$@"
	}
    elif [[ $opt = '-echo' ]]; then
	paramEcho=1
	function javacSeven()
	{   echo "$@"
	}
    elif [[ $opt = '-q' ]]; then
	warns=''
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
( javacSeven $warns -cp .:bin$jars $params $(find src | grep '.java$')  2>&1
) | colourise
