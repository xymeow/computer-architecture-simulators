Array.prototype.indexOf = function(val) {              
    for (var i = 0; i < this.length; i++) {  
        if (this[i] == val) return i;  
    }  
    return -1;  
}; 

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
		this.mem = new Memory(8, name);
		this.dir = new Array(8);
		for (var i = 0; i < this.block.length; i ++)
			this.block[i] = new Block();
		for (var i = 0; i < 8; i ++)
			this.dir[i] = new Dir();
	}
	setstatus(blockAddr, status) {
		this.block[blockAddr%this.size].status = status;
		if (status === STATUS.INVALID){
			this.block[blockAddr%this.size].data = '';
			this.block[blockAddr%this.size].blockAddr = -1;
		}
	}
	read(blockAddr, allcpu) {
		var ishit = false;
		if (this.block[blockAddr%this.size].blockAddr === blockAddr)
			ishit = true;
		var cpui, cpuj;
		cpui = blockAddr%4;
		cpuj = Math.floor(blockAddr/4); 
		var hostcpu = allcpu['cpu'+(cpui+1)];
		// alert(hostcpu.name);
		if (hostcpu.dir[cpuj].status === STATUS.MODIFIED) {
			var tcpu = allcpu[hostcpu.dir[cpuj].dirlist[0]];
			tcpu.writeback(hostcpu, blockAddr, allcpu);
		}
		// alert(hostcpu.dir[cpuj].dirlist.indexOf(this.name));
		if (hostcpu.dir[cpuj].dirlist.indexOf(this.name) === -1){
			hostcpu.dir[cpuj].dirlist.push(this.name);
			// alert(hostcpu.dir[cpuj].dirlist[0]);
		}
		hostcpu.dir[cpuj].status = STATUS.SHARED;
		this.block[blockAddr%this.size].data = hostcpu.mem.data[cpuj];
		this.block[blockAddr%this.size].blockAddr = blockAddr;
		this.setstatus(blockAddr, STATUS.SHARED);
		return ishit;
	}
	write(blockAddr, data, allcpu) {
		var ishit = false;
		if (this.block[blockAddr%this.size].blockAddr === blockAddr)
			ishit = true;
		var cpui, cpuj;
		cpui = blockAddr%4;
		cpuj = Math.floor(blockAddr/4); 
		var hostcpu = allcpu['cpu'+(cpui+1)];
		this.block[blockAddr%this.size].data = data;
		this.block[blockAddr%this.size].blockAddr = blockAddr;
		// alert(cpuj);
		if (hostcpu.dir[cpuj].status === STATUS.MODIFIED) {
			//alert('aaa');
			var tcpu = allcpu[hostcpu.dir[cpuj].dirlist[0]];
			// alert(tcpu.name);
			tcpu.writeback(hostcpu, blockAddr, allcpu);
		}
		for (var tcpu in hostcpu.dir[cpuj].dirlist) {
			var name = hostcpu.dir[cpuj].dirlist[tcpu]
			if (name !== this.name)
				allcpu[name].setstatus(blockAddr, STATUS.INVALID);
		}
		hostcpu.dir[cpuj].status = STATUS.MODIFIED;
		hostcpu.dir[cpuj].dirlist = [this.name];
		//bus.invalidate(this.name, blockAddr, allcpu, mem);
		this.setstatus(blockAddr, STATUS.MODIFIED);
		return ishit;
	}
	writeback(host, addr, allcpu) {
		var blockAddr = Math.floor(addr/4);
		host.mem.write(blockAddr, this.block[addr%this.size].data);
		this.setstatus(addr, STATUS.SHARED);
		host.dir[blockAddr].status = STATUS.SHARED;
		host.dir[blockAddr].dirlist = [this.name];
	}
	reset() {
		for (var i = 0; i < this.block.length; i ++) {
			this.block[i].data = '';
			this.block[i].blockAddr = -1;
			this.block[i].status = STATUS.INVALID;
			this.dir[i].status = STATUS.INVALID;
			this.dir[i].dirlist = [];
		}
		this.mem.reset();
	}
	
}