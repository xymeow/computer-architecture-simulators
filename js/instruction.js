class Instruction {
	constructor(pc, type, para) {
		this.pc = pc;
		this.type = type;
		this.para = para;
		this.time = type.cycles;
		this.issueTime = -1;
		this.execTime = -1;
		this.wbTime = -1;
	}
}