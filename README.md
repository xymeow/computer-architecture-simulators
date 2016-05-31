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
