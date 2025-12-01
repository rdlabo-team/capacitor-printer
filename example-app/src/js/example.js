import { Printer } from '@rdlabo/capacitor-printer';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    Printer.echo({ value: inputValue })
}
