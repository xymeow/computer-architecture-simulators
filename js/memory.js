class Memory {
	constructor (size) {
		this.data = new Array(size);
		this.size = size;
		for (var i = 0; i < this.data.length; i++)
			this.data[i] = 0;
	}
	// _check_addr (addr) { //检查地址下标
	// 	if (addr >= this.size || addr < 0) {
	// 		throw new Error('[MEM] Invalid address "' + addr + '"');
	// 	}
	// }
	// load (addr) {
	// 	this._check_addr(addr);
	// 	return this.data[addr];
	// }
	load (addr) {
		return 'M['+addr+']';
	}
}