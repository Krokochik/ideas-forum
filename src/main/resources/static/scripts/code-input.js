class TwoFactorCode {
    constructor(root) {
        this.root = root;
        this.numberOfInputs =
            root.dataset.fields !== undefined ?
                parseInt(root.dataset.fields, 10) :
                4;
        this.color =
            root.dataset.color !== undefined ? root.dataset.color : "#000";
        this.style = root.dataset.style !== undefined ? root.dataset.style : "";
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
                loader.style.marginTop = (modalNeck.offsetHeight - modalHeader.offsetHeight) / 2 - loader.offsetHeight + "px";

                const minRequestTimeMs = 1000;
                let requestMs = 0;

                try {
                    let interval = setInterval(function () {
                        requestMs++;
                    }, 1);

                    setTimeout(function () {
                        clearInterval(interval);
                    }, minRequestTimeMs);

                    await new Promise((resolve, reject) => {
                        $.ajax({
                            url: "/mfa/activate",
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
                    });

                    await sleep(minRequestTimeMs - requestMs > 0 ? minRequestTimeMs - requestMs : 1);
                    return true;
                } catch (e) {
                    await sleep(minRequestTimeMs - requestMs > 0 ? minRequestTimeMs - requestMs : 1);
                    return false;
                }
            };

            const badCode = () => {
                changeScreen();
                this.inputs[0].focus();
            };

            if (await isCodeRight(this.code)) {
                console.log('success');
                loader.style.display = 'none';
                $("#modal-body").append('<svg height="100px" width="200px" version="1.1" id="Layer_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512" xml:space="preserve" fill="#000000" stroke="#000000" stroke-width="0.00512"><g id="SVGRepo_bgCarrier" stroke-width="0"></g><g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g><g id="SVGRepo_iconCarrier"> <path style="fill:#FFE7E5;" d="M229.581,328.303c-1.852-4.966-6.673-8.303-11.981-8.303h-75.571l-2.876-8.832 c-3.456-10.598-6.144-21.99-7.979-33.852l-2.901-18.731l18.458,4.309c16.777,3.925,36.318,5.905,58.069,5.905 c17.109,0,24.252-11.87,29.705-29.687c2.987-9.711,4.787-14.276,8.218-17.502l3.695-3.465h3.516 c1.801-0.367,3.831-0.546,6.067-0.546c1.732,0,3.243,0.171,4.625,0.512l3.516,0.879l2.551,2.594 c2.654,2.697,4.403,6.562,8.346,16.802c5.914,15.377,13.278,30.413,32.162,30.413c21.82,0,41.387-1.997,58.172-5.931l18.458-4.335 l-2.884,18.731c-1.749,11.383-4.506,22.81-8.201,33.954L369.843,320H294.4c-5.308,0-10.129,3.337-11.981,8.303L256,398.746 L229.581,328.303z"></path> <path style="fill:#6D7584;" d="M256,217.6c-92.476,0-192-20.028-192-64c0-20.198,20.352-36.369,60.476-48.068l16.529-4.813 l-0.145,17.22c-0.043,4.915-0.06,9.754-0.06,14.336c0,4.796,2.645,9.139,6.886,11.358C149.461,144.538,192.435,166.4,256,166.4 s106.539-21.862,108.339-22.793c4.215-2.202,6.861-6.545,6.861-11.341c0-4.582-0.017-9.429-0.06-14.353l-0.145-17.22l16.529,4.813 C427.648,117.222,448,133.402,448,153.6C448,197.572,348.476,217.6,256,217.6z"></path> <g> <path style="fill:#212529;" d="M204.8,268.8c-27.511,0-111.206,0-114.901-60.339l-1.22-19.908l18.603,7.185 c38.204,14.754,90.334,19.439,118.016,20.924l17.118,0.913l-5.743,16.154c-0.666,1.886-1.271,3.772-1.877,5.632 C231.023,251.085,225.306,268.8,204.8,268.8z"></path> <path style="fill:#212529;" d="M307.2,268.8c-20.42,0-27.648-18.202-32.435-30.251l-1.638-4.087l-6.912-16.751l18.099-0.905 c28.271-1.417,81.527-6.042,120.388-21.069l18.611-7.194l-1.22,19.917C418.406,268.8,334.711,268.8,307.2,268.8z"></path> </g> <polygon style="fill:#A7B0C0;" points="33.442,499.2 67.575,396.8 120.09,396.8 81.69,320 226.466,320 256,398.746 285.534,320 430.31,320 391.91,396.8 444.425,396.8 478.558,499.2 "></polygon> <path style="fill:#919CB0;" d="M256,166.4c-63.838,0-106.522-21.854-108.314-22.784l-6.886-3.584v-7.765 c0-53.222,0-119.467,53.76-119.467c28.535,0,36.796,17.212,42.257,28.595c4.779,9.958,6.758,14.071,19.183,14.071 s14.396-4.113,19.183-14.071c5.47-11.383,13.722-28.595,42.257-28.595c53.76,0,53.76,66.244,53.76,119.467v7.765l-6.886,3.584 C362.522,144.546,319.838,166.4,256,166.4z"></path> <path style="fill:#212529;" d="M30.43,501.367C35.243,508.041,42.974,512,51.2,512h409.6c8.226,0,15.957-3.959,20.77-10.633 c4.813-6.673,6.118-15.258,3.516-23.066l-25.6-76.8C456.004,391.049,446.217,384,435.2,384h-22.579l19.874-39.748 c3.968-7.936,3.541-17.357-1.118-24.909c-4.668-7.552-12.902-12.143-21.777-12.143h-22.485c3.763-13.099,6.443-26.795,7.851-41.114 c26.428-12.672,40.235-32.947,40.235-61.286c0-3.371-0.384-6.502-0.93-9.54c16.017-10.487,26.53-23.987,26.53-41.66 c0-32.939-35.917-51.575-77.739-62.31C380.16,46.054,368.077,0,317.44,0c-36.591,0-48.213,24.218-53.803,35.857 c-1.058,2.21-2.637,5.495-3.507,6.562c-0.009-0.009-0.043-0.009-0.085-0.009c-0.333,0-1.638,0.256-4.045,0.256 c-3.362,0-4.574-0.503-4.582-0.512c-0.265-0.478-1.946-3.985-3.055-6.306C242.773,24.218,231.151,0,194.56,0 c-50.645,0-62.729,46.063-65.621,91.307C87.125,102.05,51.2,120.661,51.2,153.6c0,17.715,10.556,31.232,26.624,41.728 c-0.503,3.055-1.024,6.11-1.024,9.472c0,28.117,13.594,48.299,39.612,60.996c1.425,14.413,4.25,28.203,8.047,41.404H102.4 c-8.875,0-17.109,4.591-21.777,12.143c-4.668,7.552-5.086,16.973-1.118,24.909L99.379,384H76.8 c-11.017,0-20.804,7.049-24.286,17.502l-25.6,76.8C24.311,486.11,25.617,494.694,30.43,501.367z M307.2,256 c-13.158,0-16.87-13.372-22.246-26.419c32.87-1.647,85.205-6.775,124.365-21.905C406.374,255.872,332.288,256,307.2,256z M256,230.4 c1.101,0,1.545,0.137,1.553,0.137c1.229,1.246,3.925,8.252,5.53,12.433c4.821,12.535,14.865,38.63,44.117,38.63 c23.774,0,43.913-2.244,61.099-6.281c-1.715,11.17-4.343,21.76-7.697,31.881H294.4c-10.667,0-20.224,6.622-23.97,16.614L256,362.291 l-14.43-38.477c-3.746-9.993-13.295-16.614-23.97-16.614h-66.287c-3.294-10.129-5.769-20.736-7.484-31.846 c17.161,4.019,37.257,6.246,60.971,6.246c30.063,0,38.4-27.153,41.958-38.741c1.203-3.934,3.226-10.505,4.727-11.921l0,0 C251.529,230.938,252.766,230.4,256,230.4z M194.56,25.6c40.96,0,20.48,42.667,61.44,42.667S276.48,25.6,317.44,25.6 s40.96,55.185,40.96,106.667c0,0-40.96,21.333-102.4,21.333s-102.4-21.333-102.4-21.333C153.6,82.261,153.6,25.6,194.56,25.6z M76.8,153.6c0-13.943,19.61-26.547,51.26-35.78c-0.043,4.958-0.06,9.83-0.06,14.447c0,9.54,5.308,18.295,13.773,22.707 c1.903,0.99,47.292,24.226,114.227,24.226s112.324-23.236,114.227-24.226c8.465-4.412,13.773-13.167,13.773-22.707 c0-4.617-0.017-9.498-0.06-14.455c31.659,9.225,51.26,21.845,51.26,35.789c0,28.279-80.23,51.2-179.2,51.2S76.8,181.879,76.8,153.6z M102.673,207.676c38.246,14.78,88.951,20.002,121.941,21.76C219.964,242.526,218.01,256,204.8,256 C179.712,256,105.626,255.872,102.673,207.676z M76.8,409.6h64l-38.4-76.8h115.2L256,435.2l38.4-102.4h115.2l-38.4,76.8h64 l25.6,76.8H51.2L76.8,409.6z"></path> </g></svg>');
                $("#modal-body").append('<h6 style="font-size: 1.5rem;margin-top: .7rem;">Псссс!..</h6>' +
                                    '<span style="display: block;width: 70%;font-size: 1.1rem;margin: auto;text-wrap: balance;">Сделка окончена. Теперь ваш аккаунт под моей защитой</span>' +
                                '<button onclick="window.location.reload()" class="mt-4 text-bg-secondary btn" style="max-width: 55%;min-width: 45%;">Принято</button>');
                document.getElementById("no").onclick = function() {
                    window.location.reload();
                };            
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