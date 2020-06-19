Source code:
---------------------------------------
You can clone the repository PROPER and it can run in eclipse. Require jdk1.8, Other libaray can be imported from lib folder.  

Syntax specifcation:
---------------------------------------
```
program    := typeSpecifier main{stmt*}  
stm        := assign | condStmt |while
assign     := intAssign | realAssign  
condStmt   := ifStmt | ifElseStmt
while      := while (test) stmt*
intAssign  := intVar=intConst |intVar ~ intRandom  
realAssign := realVar=realConst | realVar ~ realRandom  
intRandom  := uniformInt(intConst,intConst)  
             | Bernoulli(intConst,intConst)
             ...
realRandom := uniformReal(realConst,realConst)  
             | Gaussian(realConst,realConst)
             ...           
intExpr    := intConst | intRandom | intExpr ± intExpr 
              intConst * intExpr | intExpr / intConst
ralExpr    := realConst | realRandom | realExpr ± realExpr 
              realConst * realExpr | realExpr / realConst
boolExpr   := true | false | boolExpr ∧ boolExpr 
              intConst relop intExpr | realExpr relop realExpr      
relop      := < | > | ≥ |≤ | ==  
```
The list syntax has been used in the PROPER. We may add some other syntax in the future.  

How to use
---------------------------------------
Your run by eclipse or run demo/PROPER.jar directly.  

The user interface consists of four parts: Menu bar, Config, Editor and Console.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/interface.png" width="750"> 

You can write directly in the editor or import local files.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/file.png" width="400">

We put several simple examples in here, you can try them.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/example.png" width="400"> 

First, check the syntax specification. If it's accept, then, Click "Run"->"Termination Analysis" or "Run"->"Assertion Analysis",   
the analysis results will be printed to the console.    
<img src="https://github.com/Healing1219/PROPER/blob/master/img/run.png" width="400">


Example
---------------------------------------
### Termination Analysis  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/termination.png" width="750">

### Assertions Analysis  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/assertion.png" width="750">

