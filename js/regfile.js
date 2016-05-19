class RegFile {
	constructor (count) {
		this.regs = new Array(count);
		for (var i = 0; i < this.regs.length; i++) {
			this.regs[i] = 0;
		}
		this.count = count;
	}
	_checkIndex(name) {
		// name = name.toUpperCase();
		var index = parseInt(name.substring(1));
		if (index >= 0 && index < this.count) {
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