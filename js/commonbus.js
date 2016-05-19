class CommonDataBus {
	constructor() {
		this._busy = {};
		this._result = {};
	}
	getBusy(type, name) {
		var res = this._busy[type+'.'+name];
		if (typeof res === 'undefined') {
			return null;
		}
		else
			return res;
	}
	setBusy(type, name, inst) {
		this._busy[type+'.'+name] = inst;
	}
	getResult(station) {
		var res = this._result[''+station.instruction.pc];
		if (typeof res === 'undefined') 
			return null;
		return res;
	}
	setResult(station, value) {
		this._result[''+station.instruction.pc] = value;
	}
	clearResult() {
		this._result = {};
	}
}