var STATUS = {
	'INVALID': 0,
	'SHARED': 1,
	'MODIFIED': 2
}

class CPU {
	constructor(size, name) {
		this.size = size;
		this.name = name;
		this.block = new Array(size);
		for (var i = 0; i < this.block.length; i ++)
			this.block[i] = new Block();
	}
	setstatus(blockAddr, status) {
		this.block[blockAddr%this.size].status = status;
		if (status === STATUS.INVALID){
			this.block[blockAddr%this.size].data = '';
			this.block[blockAddr%this.size].blockAddr = -1;
		}
	}
	read(blockAddr, bus, mem, allcpu) {
		var ishit = false;
		if (this.block[blockAddr%this.size].blockAddr === blockAddr)
			ishit = true;
		this.block[blockAddr%this.size].data = bus.read(this.name, blockAddr, mem, allcpu);
		this.block[blockAddr%this.size].blockAddr = blockAddr;
		this.setstatus(blockAddr, STATUS.SHARED);
		return ishit;
	}
	write(blockAddr, data, allcpu, bus, mem) {
		var ishit = false;
		if (this.block[blockAddr%this.size].blockAddr === blockAddr)
			ishit = true;
		this.block[blockAddr%this.size].data = data;
		this.block[blockAddr%this.size].blockAddr = blockAddr;
		bus.invalidate(this.name, blockAddr, allcpu, mem);
		this.setstatus(blockAddr, STATUS.MODIFIED);
		return ishit;
	}
	reset() {
		for (var i = 0; i < this.block.length; i ++) {
			this.block[i].data = '';
			this.block[i].blockAddr = -1;
			this.block[i].status = STATUS.INVALID;
		}
			
	}
	
}