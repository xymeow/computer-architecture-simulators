class Instruction {
	constructor(pc, type, para) {
		this.pc = pc;
		this.type = type;
		this.para = para;
		this.time = type.cycles;
		this.execFlag = false;
		this.issueTime = -1;
		this.execSTime = -1;
		this.execTime = -1;
		this.wbTime = -1;
	}
}