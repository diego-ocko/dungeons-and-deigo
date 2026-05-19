
import { instantiate } from './dungeons-and-deigo-webApp.uninstantiated.mjs';
import "./custom-formatters.js"

const exports = (await instantiate({
})).exports;

export const {
memory,
_initialize
} = exports


