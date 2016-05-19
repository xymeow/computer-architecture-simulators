// var InstructionType, ReservationStation, Instruction;
// InstructionType = require('./instruction_type.js');
// Instruction = require('./instruction.js');

// ReservationStation = require('./reservation_station.js');

class Main {
	constructor (instruction, system) {
		var pc = 0;
		this.system = system;
		this.system.clock = 0;

		instruction = instruction.toUpperCase().replace(/[\s,]+/g, ',').replace(/^,|,$/g, '');
		var tokens = instruction.split(',');
		var inst = [];

		for (var i = 0; i < tokens.length;) {
			var instructionType = this.system.instructionTypes[tokens[i++]];
			var para = [];
			for (var j = 0; j < instructionType.para.length; i++) {
				switch (instructionType.para[j++]) {
					case InstructionType.PARA_REG:
						para.push(tokens[i]); break;
					case InstructionType.PARA_ADDR:
						para.push(parseInt(tokens[i])); break;
				}
			}
			inst.push(new Instruction(++pc, instructionType, para));
		}
		this.instructions = inst;
		this.issuedInst = 0;
	}

	step () {
		this.system.clock ++;
		if (this.instructions.length > this.issuedInst) { //issue
			var inst = this.instructions[this.issuedInst];
			var stations = inst.type.station;
			for (var i = 0; i < stations.length; i++) {
				if (stations[i].state == STATUS.IDLE) {
					var station = stations[i];
					station.state = STATUS.ISSUE;
					station.instruction = inst;
					// alert(inst.pc);
					inst.issueTime = this.system.clock;
					var dest = inst.type.destPara;
					var paraCount = inst.type.para.length;
					station.para = [];
					station.tags = [];
					for (var j = 0; j < paraCount; j++) {
						var value = null;
						var tag = null;
						if (j != dest) {
							switch (inst.type.para[j]) {
								case InstructionType.PARA_REG:
									tag = this.system.commonDataBus.getBusy(InstructionType.PARA_REG, inst.para[j]);
									if (tag == null) {
										value = this.system.regFile.getReg(inst.para[j]);
									}
									break;
								case InstructionType.PARA_ADDR:
									tag = this.system.commonDataBus.getBusy(InstructionType.PARA_ADDR, inst.para[j]);
									value = inst.para[j];
									break;
							}
						}
						else {
							value = inst.para[j];
						}
						station.para.push(value);
						station.tags.push(tag);
						
					}
					var type = inst.type.para[dest];
					var name = inst.para[dest];
					this.system.commonDataBus.setBusy(type, name, station);
					this.issuedInst ++;
					break;
					// alert(this.issuedInst);
				}
			}
		}

		for (var i in this.system.reservationStation) {
			var station = this.system.reservationStation[i];
			if (station.state === STATUS.EXEC) {
				// alert(station.instruction);
				if ((--station.instruction.time) === 0) { // *********
					station.instruction.execTime = this.system.clock;
					station.state = STATUS.WRITE;
				}
			}
			else if (station.state === STATUS.WRITE) {
				var dest = station.instruction.type.destPara;
				var type = station.instruction.type.para[dest];
				var name = station.instruction.para[dest];
				var value = station.instruction.type.act.call(station, station.para);
				if (typeof value === 'undefined') {
					value = true;
				}
				if (this.system.commonDataBus.getBusy(type, name) === station) {
					switch (type) {
						case InstructionType.PARA_REG:
							this.system.regFile.setReg(name, value);
							break;
						case InstructionType.PARA_ADDR:
							value = name;
							break;
					}
					this.system.commonDataBus.setBusy(type, name, null);
					this.system.commonDataBus.setResult(station, value);
				}
				station.instruction.wbTime = this.system.clock;
				station.state = STATUS.IDLE;
			}
		}
		var done = true;
		for (var i in this.system.reservationStation) {
			var station = this.system.reservationStation[i];
			if (station.state === STATUS.ISSUE) {
				var needMore = false;
				for (var j = 0; j < station.tags.length; j++) {
					if(station.tags[j] !== null) {
						var value = this.system.commonDataBus.getResult(station.tags[j]);
						if (value != null) {
							station.para[j] = value;
							station.tags[j] = null;
						}
						else
							needMore = true;
					}
				}
				if (!needMore) {
					station.state = STATUS.EXEC;
					// alert(station.name);
				}
			}
		}
		if (station.state !== STATUS.IDLE) {
			done = false;
		}
		this.system.commonDataBus.clearResult();
		return done;
	}

	run () {
		while (!this.step());
	}
	// reset () {
	// 	this.system.clock = 0;
	// }
}


