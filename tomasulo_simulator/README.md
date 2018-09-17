# Tomasulo algorithm simulator
A small project I made when I was taking the course Computer Architecture.

### Run
1. Just clone it and click index.html to see. (I think most web brouser support ES6 today)

or

2. clone it and use electron to run it as a desktop app.

### Usage

The 'system' has 15 interger registers (r0-r15), 15 float registers (f0-f15), 5 reservation stations.

Supported operations: addd/subd/muld/divd/ld

inst format: `op dest, source1, source2`

Here is an example:

	ld f8, 21, r3   // load address (r3) + 21 in memory into register F8
	ld f4, 16, r4	// load address (r4) + 16 in memory into register F4
	muld f2, f4, f6	// multiply f4 and f6 then store the result into f2
	subd f10, f8, f4 	// f8-f4 -> f10
	divd f12, f2, f8 	// f2/f8 -> f12
	addd f8, f10, f4 	// f10+f4 -> f8

Then set the cost time of each type of operation, click 'Start'

You can either step in or run through the instructions.

---
# Tomasulo算法模拟器

我的体系结构实验，用js实现
***
###运行方法
1. 点击index.html即可，推荐使用chrome浏览器（由于使用了ES6的特性，旧版的浏览器可能不兼容。）

2. 从electron启动：若安装了electron，在终端输入`electron .`即可（我比较懒不想打包了）

###指令支持
本模拟器支持 addd/subd/muld/divd/ld指令，系统设定有整数寄存器r0~r15和浮点寄存器f0~f15

指令格式为：op dest, source1, source2

指令解析是使用了正则表达式，而且我貌似还没有做合法性检查（因为懒），请按照格式输入＝。＝

示例的指令为：

	ld f8, 21, r3
	ld f4, 16, r4
	muld f2, f4, f6
	subd f10, f8, f4
	divd f12, f2, f8
	addd f8, f10, f4
	


其中ld指令的source1为整数，表示偏移量，source2为整数寄存器名

_本程序也许存在少许bug，大家看看就好_
