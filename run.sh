#!/bin/sh

## program execution information
package="se.kth.maandree.ponypipe"
hasMain=1
hasHome=0


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


## libraries
jars=''
if [ -d lib ]; then
    jars=`echo $(find lib | grep .jar$) | sed -e 's/lib\//:/g' -e 's/ //g'`
fi


## default runs
runs=''
if [[ $hasMain = 1 ]]; then
    runs+='main main-da'
    if [[ $hasHome = 1 ]]; then
	runs+='falsehome'
    fi
fi

## custom runs
runs+=''


## default run
if [[ $# = 0 ]]; then
    javaSeven -ea -cp bin$jars "$package".Program


## custom runs

elif [[ $hasMain  &&  $1 = "main" ]]; then
    javaSeven -ea -cp bin$jars "$package".Program

elif [[ $hasMain  &&  $1 = "main-da" ]]; then
    javaSeven -da -cp bin$jars "$package".Program
    
elif [[ $hasMain  &&  $hasHome  &&  $1 = "falsehome" ]]; then
    __myhome=$HOME
    HOME='/dev/shm'
    javaSeven -ea -cp bin$jars "$package".Program
    HOME=$__myhome


## completion
elif [[ $1 = "--completion--" ]]; then
    _run()
    {
	local cur prev words cword
	_init_completion -n = || return
	
	COMPREPLY=( $( compgen -W "$runs" -- "$cur" ) )
    }
    
    complete -o default -F _run run

## missing rule
else
    echo "run: Rule missing.  Stop." >&2
fi
