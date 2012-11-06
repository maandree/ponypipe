Utility for ponifing (and deponifing) text.

It replaces words such as ’everyone’ with ’everypony’.


To ponify a file run:

    ponypipe --ponify < SOURCE > TARGET

OR

    ponypipe < SOURCE > TARGET

To deponify a file run:

    ponypipe --deponify < SOURCE > TARGET

You can use customised replacement rules by adding the option:

    --rules RULES

OR

    --rule RULES


`-r` can be used instead of `--rules`.

`-d` can be used instead of `--deponify`.

`-z` can be used instead of `--ponify`.


Features:

* Adaptive casing [todo: can be made better]
* Clopen words [todo: make it possible to have open beginnings]
* On the fly ponification

Todo:s:

* Make it possible to make ponified words highlighted
* Extend the list so that clopen words does not match to known bad matchings (for example, add, `boyle :: boyle`)
