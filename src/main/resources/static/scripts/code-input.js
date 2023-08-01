class TwoFactorCode {
    constructor(root) {
        this.root = root;
        this.numberOfInputs =
            root.dataset.fields != undefined ?
            parseInt(root.dataset.fields, 10) :
            4;
        this.color =
            root.dataset.color != undefined ? root.dataset.color : "#000";
        this.style = root.dataset.style != undefined ? root.dataset.style : "";
        this.autoSubmit = root.dataset.submit !== "btn";
        this.code = Array(this.numberOfInputs).fill("");
        this.inputs = undefined;
        this.form = undefined;
        this.editable = true;

        this.onInput = this.onInput.bind(this);
        this.onKeydown = this.onKeydown.bind(this);
        this.onFocus = this.onFocus.bind(this);
        this.onInvalid = this.onInvalid.bind(this);
    }

    generateHTML() {
        if (this.style == "") this.style = "color:" + this.color;
        else if (!this.style.includes("color:"))
            this.style += ";color:" + this.color;
        const inputs = Array.from({
                    length: this.numberOfInputs
                },
                (_, index) =>
                `<input
              data-index="${index}"
              aria-label="input for code at position ${index}"
              type="text"
              title=""
              style="${this.style}"
              required
             />`
            )
            .join("")
            .trim();

        if (this.autoSubmit)
            return `
             <form>
                <div class="two-factor-code-inputs">${inputs}</div>
             </form>`;
        else {
            return `
            <form>
                <div class="two-factor-code-inputs">${inputs}</div>
                <button type="submit" class="submit-btn">Подтвердить<button/>
             </form>`;
        }
    }

    render() {
        this.container = document.createElement("div");
        this.container.classList.add("two-factor-code");
        this.container.innerHTML = this.generateHTML();

        this.inputs = this.container.querySelectorAll(".two-factor-code input");
        this.form = this.container.querySelector("form");

        this.root.appendChild(this.container);

        this.addEventListeners();
    }

    addEventListeners() {
        this.container.addEventListener("input", this.onInput);
        this.container.addEventListener("keydown", this.onKeydown);

        this.inputs.forEach((input) => {
            input.addEventListener("invalid", this.onInvalid);
            input.addEventListener("focus", (event) => this.onFocus(event));
        });
    }

    async submitCode() {
        if (this.form.checkValidity()) {
            console.log(this.code);

            this.editable = false;
            this.inputs.forEach((input) => {
                input.blur();
                input.readOnly = true;
            });

            let modalNeck = document.getElementById("modal-neck");
            let modalBody = document.getElementById("modal-body");
            let modalHeader = document.getElementById("modal-header");
            let loader = document.getElementById("loader-2");
            let msg = document.getElementById("msg0");
            let displayProperties = {};
            let currentScreen = 0;

            const changeScreen = () => {
                let children = Array.from(modalBody.childNodes)
                    .filter(child => child.nodeType === 1);
                loader.style.marginTop = modalNeck.style.marginTop - modalHeader.style.marginTop / 2 - loader.clientHeight;
                if (currentScreen == 0) {
                    children.forEach(child => {
                        displayProperties[child.id] = child.style.display;
                        child.style.display = child.style.display === "none" ? "block" : "none";
                    });
                    msg.style.display = "none";
                    currentScreen = 1;
                } else if (currentScreen == 1) {
                    children.forEach(child => {
                        child.style.display = displayProperties[child.id] || "block";
                    });
                    msg.style.display = "inline-table";
                    currentScreen = 0;
                }
            }

            const isCodeRight = async (code) => {
                let height = document.getElementById("modal-neck").offsetHeight;
                changeScreen();
                document.getElementById("modal-neck").style.height = height + "px";

                const minRequestTimeMs = 1000;
                let requestMs = 0;



                try {
                    let interval = setInterval(function () {
                        requestMs++;
                    }, 1);
    
                    setTimeout(function () {
                        clearInterval(interval);
                    }, minRequestTimeMs);

                    new Promise((resolve, reject) => {
                        $.ajax({
                            url: "/mfa/activate/",
                            method: "POST",
                            async: true,
                            data: {
                                PIN: code.join(""),
                            },
                            success: (response) => {
                                resolve(response);
                            },
                            error: (error) => {
                                reject(error);
                            }
                        });
                    }).then(async (response) => {
                        await sleep(minRequestTimeMs - requestMs > 0? minRequestTimeMs - requestMs : 1);
                        resolve(true);
                    }).catch(async (error) => {
                        await sleep(minRequestTimeMs - requestMs > 0? minRequestTimeMs - requestMs : 1);
                        resolve(false);
                    });

                    return true;
                } catch (e) {
                    return false;
                }
            };

            const badCode = () => {
                changeScreen();
                this.inputs[0].focus();
            };

            if (await isCodeRight(this.code)) {
                console.log('success');
            } else {
                badCode();
                this.editable = true;
                this.inputs.forEach((input) => {
                    input.value = "";
                });
                this.inputs.forEach((input) => {
                    input.dispatchEvent(new Event("invalid"));
                    input.readOnly = false;
                });
                this.inputs[0].focus();
            }
        }
    }

    onInput(event) {
        if (this.editable) {
            let value = event.target.value.replace(/\s+/g, "").toUpperCase();
            let currentIndex = Number(event.target.dataset.index);

            const digitsRegex = /^\d*$/;
            const lettersRegex = /^[A-Za-z]+$/;
            const lettersAndDigitsRegex = /^[A-Za-z0-9]+$/;

            const filter = this.root.dataset.filter?.split("+") || [];
            const regex = filter.includes("num") ?
                filter.includes("ltr") ?
                lettersAndDigitsRegex :
                digitsRegex :
                filter.includes("ltr") ?
                lettersRegex :
                digitsRegex;

            if (!regex.test(value)) {
                event.target.value = this.code[currentIndex];
                return;
            }

            while (value.length > 0 && currentIndex < this.numberOfInputs) {
                this.inputs[currentIndex].value = value.slice(0, 1);
                this.code[currentIndex] = value.slice(0, 1);

                const nextIndex = ++currentIndex;

                if (nextIndex < this.numberOfInputs && value.slice(1) !== "") {
                    const nextDigit = value.slice(1);
                    this.code[nextIndex] = nextDigit;

                    const nextInput = this.inputs[nextIndex];
                    nextInput.focus();
                    nextInput.value = nextDigit;

                    value = value.slice(1);
                } else {
                    value = "";
                }
            }

            if (
                this.code.filter((str) => str.trim() !== "").length ===
                this.numberOfInputs
            ) {
                this.submitCode();
            }
        }
    }

    onKeydown(event) {
        const currentIndex = Number(event.target.dataset.index);
        const prevIndex = currentIndex > 0 ?
            currentIndex - 1 :
            currentIndex;
        const nextIndex = currentIndex < this.numberOfInputs ?
            currentIndex + 1 :
            currentIndex;
        if (event.code === "Backspace" && this.editable) {
            event.preventDefault();
            this.inputs[prevIndex].focus();
            this.inputs[currentIndex].value = "";
            this.code[currentIndex] = "";
        } else if (event.code === "ArrowLeft") {
            event.preventDefault();
            this.inputs[prevIndex].focus();
            if (event.target.value != "") {
                this.inputs[prevIndex].setSelectionRange(0, 1);
            }

        } else if (event.code === "ArrowRight") {
            event.preventDefault();
            this.inputs[nextIndex].focus();
            if (event.target.value != "") {
                this.inputs[nextIndex].setSelectionRange(0, 1);
            }
        }
    }

    onFocus(event) {
        if (!this.editable) {
            event.preventDefault();
            event.target.blur();
        } else {
            if (event.target.value != "") {
                event.target.setSelectionRange(0, 1);
            }
        }
    }

    onInvalid(event) {
        this.container
            .querySelector(".two-factor-code-inputs")
            .classList.add("submitted");
    }
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function render() {
    const root = document.getElementById("code-input");
    root.innerHTML = "";
    const code = new TwoFactorCode(root);
    code.render();
}