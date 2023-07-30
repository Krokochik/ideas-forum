class TwoFactorCode {
  constructor(root, numberOfInputs) {
    this.root = root;
    this.numberOfInputs = numberOfInputs;
    this.code = new Array(numberOfInputs).fill("");
    this.inputs = undefined;
    this.form = undefined;

    this.onInput = this.onInput.bind(this);
    this.onKeydown = this.onKeydown.bind(this);
    this.submitCode = this.submitCode.bind(this);
    this.onInvalid = this.onInvalid.bind(this);
  }

  generateHTML() {
    const inputs = new Array(this.numberOfInputs)
      .fill(undefined)
      .map((input, index) => {
        return `<input
                data-index="${index}"
                aria-label="input for code at position ${index}"
                type="text"
                required
                />`;
      });

    return `
      <form>
        <div class="two-factor-code-inputs">${inputs.join("").trim()}</div>
        <button class="submit-btn">Submit</button>
      </form>
    `;
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
    });

    this.container
      .querySelector(".submit-btn")
      .addEventListener("click", this.submitCode);
  }

  onInput(event) {
    let value = event.target.value.replace(/\s+/g, "").toUpperCase();
    let currentIndex = Number(event.target.dataset.index);

    const digitsRegex = /^\d*$/;
    const lettersRegex = /^[A-Za-z]+$/;
    const lettersAndDigitsRegex = /^[A-Za-z0-9]+$/;

    var regex = undefined;
    if ("filter" in this.root.dataset && this.root.dataset.filter.includes("+")) {
        const filter = this.root.dataset.filter.split("+");
        if (filter.includes("num"))
            if (filter.includes("ltr"))
                regex = lettersAndDigitsRegex;
            else regex = digitsRegex;
        else if (filter.includes("ltr"))
            regex = lettersRegex;
    } else regex = digitsRegex

    if (!regex.test(value)) {
      // can't prevent input from not being saved in the value cause this event doesn't handle that
      // preventDefault() also doesn't do anything on event 'input',
      // so just set the value to the old value
      event.target.value = this.code[currentIndex];
      return;
    }

    while (value.length > 0 && currentIndex < this.numberOfInputs) {
      this.inputs[currentIndex].value = value.slice(0, 1);
      this.code[currentIndex] = value.slice(0, 1);

      const nextIndex = ++currentIndex;

      if (nextIndex < this.numberOfInputs && value.slice(1) !== '') {
        const nextDigit = value.slice(1);
        console.log(nextDigit)
        this.code[nextIndex] = nextDigit;

        const nextInput = this.inputs[nextIndex];
        nextInput.focus();
        nextInput.value = nextDigit;

        value = value.slice(1);
      } else value = '';
    }

    // console.log(this.code);
  }

  onKeydown(event) {
    if (event.code === "Backspace") {
      event.preventDefault();
      const currentIndex = Number(event.target.dataset.index);
      var prevIndex = 0;
      if (currentIndex > 0)
          prevIndex = currentIndex - 1;
      else prevIndex = currentIndex;
      this.inputs[prevIndex].focus();
      this.inputs[currentIndex].value = "";
      this.code[currentIndex] = "";
    }
  }

  // add this event handler on each the inputs to add a class that will handle highlighting the
  // input borders that are empty
  onInvalid(event) {
    this.container
      .querySelector(".two-factor-code-inputs")
      .classList.add("submitted");
  }

  submitCode(event) {
    // since we're using the button as a submit
    // we don't want to actually submit the form when it's possible
    // use checkValidity() to see if we have all the inputs filled out so that we can preventDefault the submit
    if (this.form.checkValidity()) {
      event.preventDefault();

      console.log(this.code);

      if (this.code.join("") === this.answer) {

      } else {

      }
    }
  }
}

function render() {
    const root = document.getElementById("code-input");
    const code = new TwoFactorCode(root, parseInt(root.dataset.fields));
    code.render();
}