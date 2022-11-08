The details at https://stackoverflow.com/a/71835812/8246539 fixes:

```
'CodeMirror' cannot be used as a JSX component.
  Its instance type 'UnControlled' is not a valid JSX element.
    The types returned by 'render()' are incompatible between these types.
      Type 'React.ReactNode' is not assignable to type 'import("/home/matthew/Code/content-team-apps/js/pipeline-generator/node_modules/@types/react-transition-group/node_modules/@types/react/index").ReactNode'.
        Type '{}' is not assignable to type 'ReactNode'.  TS2786
```