Utility for ponifing (and deponyifing) text.

It replaces words should as ’everyone’ with ’everypony’.


To ponify a file run:

    ponypipe --ponify < SOURCE > TARGET

or

    ponypipe < SOURCE > TARGET

To deponify a file run:

    ponypipe --deponify < SOURCE > TARGET

Information will be put in stderr, to suppress this append:

    2>/dev/null

You can use customised replacement rules by adding the option:

    --rules RULES

OR

    --rule RULES


`-r` can be used instead of `--rules`.

`-d` can be used instead of `--deponify`.

`-z` can be used instead of `--ponify`.

