Array.prototype.indexOf = function(val) {              
    for (var i = 0; i < this.length; i++) {  
        if (this[i] == val) return i;  
    }  
    return -1;  
}; 

class Bus {
	constructor() {
		this.shared = {};
		this.modified = {};

	}
	writeback(cpu, addr, mem, allcpu) {
		var tcpu = allcpu[cpu];
		var size = tcpu.size;
		mem.write(addr, tcpu.block[addr%size].data);
		tcpu.setstatus(addr, STATUS.SHARED);
		if(!this.shared[addr]||typeof this.shared[addr] === 'undefined')
			this.shared[addr] = new Array();
		this.shared[addr].push(cpu);
		this.modified[addr] = null;
	}
	invalidate(cpu, addr, allcpu, mem) {
		var mod = this.modified[addr];
		if (mod)
			this.writeback(mod, addr, mem, allcpu);
		var slist = this.shared[addr];
		for (var tcpu in slist) {
			// alert(slist[tcpu]);
			if (slist[tcpu] !== cpu)
			allcpu[slist[tcpu]].setstatus(addr, STATUS.INVALID);
		}
		this.shared = [];
		this.modified[addr] = cpu;
	}
	read(cpu, addr, mem, allcpu) {
		var mod = this.modified[addr];
		if(mod)
			this.writeback(mod, addr, mem, allcpu);
		if(!this.shared[addr]||typeof this.shared[addr] === 'undefined')
			this.shared[addr] = new Array();
		if(this.shared[addr].indexOf(cpu) === -1)
			this.shared[addr].push(cpu);
		return mem.read(addr);
	}
	reset() {
		this.shared = {};
		this.modified = {};
	}
}