class RegFile {
	constructor (count, prefix) {
		this.regs = new Array(count);
		for (var i = 0; i < this.regs.length; i++) {
			this.regs[i] = 'R['+prefix+i+']';
		}
		this.count = count;
		this.prefix = prefix;
	}
	_checkIndex(name) {
		// name = name.toUpperCase();
		var index = parseInt(name.substring(1));
		if (index >= 0 && index < this.count && name.substring(0, 1) === this.prefix) {
			return index;
		}
		throw new Error('[REGFILE] Ivalid regester"' + name + '"');
	}
	getReg (name) {
		return this.regs[this._checkIndex(name)];
	}
	setReg (name, value) {
		this.regs[this._checkIndex(name)] = value;
	}
}