document.addEventListener('DOMContentLoaded', () => {

    // Animação de fade-in ao rolar a página
    const sections = document.querySelectorAll('.content-section');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                // Opcional: parar de observar depois que a animação acontece
                // observer.unobserve(entry.target); 
            }
        });
    }, {
        threshold: 0.1 // A animação começa quando 10% da seção está visível
    });

    sections.forEach(section => {
        observer.observe(section);
    });

    // Smooth scroll para links da navegação
    const navLinks = document.querySelectorAll('nav ul li a');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href');
            const targetElement = document.querySelector(targetId);
            
            if (targetElement) {
                // O offset é para compensar a altura do header fixo
                const headerOffset = 80;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Drag-to-scroll para o carrossel de imagens e indicadores de rolagem
    const slider = document.querySelector('.gallery');
    const scrollIndicatorLeft = document.querySelector('.scroll-indicator-left');
    const scrollIndicatorRight = document.querySelector('.scroll-indicator-right');

    if (slider && scrollIndicatorLeft && scrollIndicatorRight) {
        let isDown = false;
        let startX;
        let scrollLeft;

        const updateIndicators = () => {
            const tolerance = 5; // Tolerância em pixels para o cálculo final

            // Indicador esquerdo
            if (slider.scrollLeft <= tolerance) {
                scrollIndicatorLeft.style.opacity = '0'; // Esconde se estiver no início
            } else {
                scrollIndicatorLeft.style.opacity = '1';
            }

            // Indicador direito
            if (slider.scrollLeft + slider.clientWidth >= slider.scrollWidth - tolerance) {
                scrollIndicatorRight.style.opacity = '0'; // Esconde se estiver no final
            } else {
                scrollIndicatorRight.style.opacity = '1';
            }
        };

        // --- Funcionalidade de clique nas setas ---
        scrollIndicatorRight.addEventListener('click', () => {
            slider.scrollBy({ left: slider.clientWidth * 0.8, behavior: 'smooth' });
        });

        scrollIndicatorLeft.addEventListener('click', () => {
            slider.scrollBy({ left: -slider.clientWidth * 0.8, behavior: 'smooth' });
        });

        slider.addEventListener('scroll', updateIndicators);

        slider.addEventListener('mousedown', (e) => {
            isDown = true;
            slider.classList.add('active');
            startX = e.pageX - slider.offsetLeft;
            scrollLeft = slider.scrollLeft;
        });

        slider.addEventListener('mouseleave', () => {
            isDown = false;
            slider.classList.remove('active');
        });

        slider.addEventListener('mouseup', () => {
            isDown = false;
            slider.classList.remove('active');
        });

        slider.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - slider.offsetLeft;
            const walk = (x - startX) * 2; // Multiplicador para acelerar o scroll
            slider.scrollLeft = scrollLeft - walk;
        });

        // Verifica o estado inicial dos indicadores ao carregar a página
        updateIndicators();
    }
});