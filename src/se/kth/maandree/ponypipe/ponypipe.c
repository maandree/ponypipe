#define false  0
#define true   1


int main(int argc, char** argv)
{
  char* rules = "./rules";
  long ponify = true;
  long i;
  
  for (i = 1; i < argc; i++)
    {
      char* arg = *(argv + i);
      #define _(I, C) (*(arg + I) == C)
      if (_(0, '-'))
	continue;
      if (_(1, '-'))
	{
	  if (_(2, 'p') && _(3, 'o') && _(4, 'n') && _(5, 'i') && _(6, 'f') && _(7, 'y') && _(8, 0))
	    ponify = true;
	  else if (_(2, 'd') && _(3, 'e') && _(4, 'p') && _(5, 'o') && _(6, 'n') && _(7, 'i') && _(8, 'f') && _(9, 'y') && _(10, 0))
	    ponify = false;
	  else if (_(2, 'r') && _(3, 'u') && _(4, 'l') && _(5, 'e') && (_(6, 0) || (_(6, 's') && _(7, 0))))
	    if (i + 1 < n)
	      rules = *(args + ++i);
	}
      else if (_(1, 'z') || _(1, 'p'))
	ponify = true;
      else if (_(1, 'd'))
	ponify = false;
      else if (_(1, 'r'))
	if (i + 1 < n)
	  rules = *(args + ++i);
      #undef _
    }
}

