class Memory{
	constructor(size) {
		this.size = size;
		this.data = new Array(size);
		for (var i = 0; i < this.data.length; i++) {
			this.data[i] = i;
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
			this.data[i] = i;
		}
	}
}