(function () {
    const sr = window.sr = ScrollReveal()

    sr.reveal('.hero-title, .hero-paragraph, .hero-button, .contact-us-header, .contact-us-form', {
      duration: 1000,
      distance: '40px',
      easing: 'cubic-bezier(0.5, -0.01, 0, 1.005)',
      origin: 'bottom',
      interval: 150
    })

    sr.reveal('.feature', {
      duration: 600,
      distance: '40px',
      easing: 'cubic-bezier(0.5, -0.01, 0, 1.005)',
      interval: 100,
      origin: 'bottom',
      viewFactor: 0.5
    })

  document
    .querySelectorAll("a[onclick-scroll-into-view]")
    .forEach(function(el) {
      let selector = el.getAttribute("onclick-scroll-into-view");
      let element = document.querySelector(selector);

      el.addEventListener("click", function () { element.scrollIntoView({ behavior: 'smooth' }) });
    });

  document
    .querySelector("form.contact-us-form")
    .addEventListener("submit", function (ev) {
      ev.preventDefault();
      this.style.display = "none";

      let formData = new FormData(this);
      let formDataObject = Object.fromEntries(formData.entries());
      let formDataJsonString = JSON.stringify(formDataObject);

      let url = this.getAttribute('action');
      let options = {
          method: this.getAttribute('method'),
          body: formDataJsonString,
          headers: {'content-type': 'application/json'},
      };

      fetch(url, options)
          .then(value => {
            let selector = value.ok ? ".contact-us-form-success" : ".contact-us-form-error";
            let element = document.querySelector(selector);

            element.style.display = "block";
          })
          .catch(_ => document.querySelector(".contact-us-form-error").style.display = "block");
    });

}())
