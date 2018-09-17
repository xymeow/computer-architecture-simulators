class InstructionType {
	constructor (name, cycles, destPara, para, act, station) {
		this.name = name;
		this.cycles = cycles;
		this.para = para;
		this.destPara = destPara;
		this.act = act;
		this.station = station;
		
	}
}

InstructionType.PARA_REG = 0;
InstructionType.PARA_ADDR = 1;
InstructionType.PARA_REG_R = 2;