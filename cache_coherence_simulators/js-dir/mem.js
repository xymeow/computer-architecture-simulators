class Memory{
	constructor(size, name) {
		var id;
		switch(name) {
			case 'cpu1': id = 0;break;
			case 'cpu2': id = 1; break;
			case 'cpu3': id = 2; break;
			case 'cpu4': id = 3; break;
		}
		this.id = id;
		this.size = size;
		this.data = new Array(size);
		for (var i = 0; i < this.data.length; i++) {
			this.data[i] = i*4+id;
		}
	}
	read(addr) {
		return this.data[addr];
	}
	write(addr, data) {
		this.data[addr] = data;
	}
	reset() {
		for (var i = 0; i < this.data.length; i++) {
			this.data[i] = i*4+this.id;
		}
	}
}