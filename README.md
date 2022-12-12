# HovelData
Hovel Data is the data management system for single table databases. Such databases consists of several distributed files. One such file for one dataset.

Macro processor supports reverence resolution.
```
%let n=name;
```
will create macro variable *n* with value *name*
and then reference
```
&n
```
will be resolved as *name*.

