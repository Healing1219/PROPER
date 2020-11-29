## Source code:

You can clone the repository PROPER and it can run in eclipse. Require jdk1.8, Other libaray can be imported from lib folder.

## Syntax specifcation:

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

## Install

For Linux：

1.Download and unzip the folder "PROPER" into the path `$HOME`.

2.Enter the "$HOME/PROPER/config" directory and execute `source lingo13.sh` 和 `source lpsolve.sh`. 
If "PROPER" is placed in a location is not $HOME, you need to modify the path in 'lingo13.sh' and 'lpsolve.sh'.For details, please refer to "config/configure.txt" document.

3.Since the program will read and write files during execution, `sudo Chmod 777 *` can be executed outside the "PROPER" folder.
After the configuration is completed, execute the command`java -jar PROPER_linux.jar` in "PROPER" directory。
Similarly, the .jar files in the "demo/assertions" and "demo/termination" folders can be executed in the same way.

For Windows: 

1.Support JavaFX

2.Install LINGO (the installation package has provided "config/lingo-windows-64x86-18.0.exe"), and add "lingo.exe" directory to the `$PATH `environment variable

3.Copy 'lpsolve55j.dll' and 'lpsolve55.dll' to the `windows/system32` directory，add `lpsolve55j.jar` to Java development environment variable `CLASSPATH`,
（"config/lp_solve_5.5_java" includes jar package`lpsolve55j.jar`, and stub package `lpsolve55j.dll`; "config/lp_solve_5.5.0.14_dev_win64.zip" includes the dynamic link library `lpsolve55.dll` of windows64 platform)

4.After the configuration is completed, you can click the "PROPER_windows.jar" directly.
(if you want to call the file in "PROPER/example" directly, the "example" directory and "PROPER_windows.jar" need to be in the same level folder)

**NOTE**：When solving the concentration problem(the probability of termination after N steps shows an exponential decrement), 
we used the LINGO tool (used to solve non-linear optimization problems). 
We used lingo18.jar for windows. But for Linux，we only found the available version of lingo13.jar, and we found that the final calculation results of N are somewhat different.

## How to use

For Linux，you need to select Termination analysis or Assertion analysis, firstly.
After selecting 1, enter the relative path of the file to be verified. As shown in the figure below, the program in the file can be printed out.
If the file and PROPER_linux.jar are in the same directory, only need to enter the file name
<img src="https://github.com/Healing1219/PROPER/blob/master/img/termination_linux.png" width="650">

After selecting 2, enter the assertion to be verified, such as' x > 0 & & Y > 0 '. Next, enter the relative path of the file to be verified, as shown in the figure below.
<img src="https://github.com/Healing1219/PROPER/blob/master/img/assertion_linux.png" width="650">

For Windows, it support graphical interface, the operation is more flexible and concise. We have a [demo_video.mp4](https://github.com/Healing1219/PROPER/blob/master/demo_video.mp4).
The user interface consists of four parts: Menu bar, Config, Editor and Console.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/interface.png" width="750">

You can write directly in the editor or import local files.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/file.png" width="400">

We put several simple examples in here, you can try them.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/example.png" width="400">

First, check the syntax specification. If it's accept, then, Click "Run"->"Termination Analysis" or "Run"->"Assertion Analysis",  
the analysis results will be printed to the console.  
<img src="https://github.com/Healing1219/PROPER/blob/master/img/run.png" width="400">

## Example

### Termination Analysis
For Linux

<img src="https://github.com/Healing1219/PROPER/blob/master/img/termination_result.png" width="750">

For Windows

<img src="https://github.com/Healing1219/PROPER/blob/master/img/termination.png" width="750">

### Assertions Analysis

For Linux

<img src="https://github.com/Healing1219/PROPER/blob/master/img/assertion_result.png" width="650">

For Windows

<img src="https://github.com/Healing1219/PROPER/blob/master/img/assertion.png" width="750">
