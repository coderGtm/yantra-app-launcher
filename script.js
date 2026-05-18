// ========================================
// Initialize AOS (Animate On Scroll)
// ========================================

AOS.init({
    duration: 800,
    easing: 'ease-out-cubic',
    once: true,
    offset: 50
});

// ========================================
// Page Load Fade In Animation
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    // Add fade-in class to body for initial load
    document.body.classList.add('page-loaded');

    // Animate hero elements on load
    const heroElements = document.querySelectorAll('.hero-badge, .hero-title, .hero-subtitle, .hero-cta, .hero-stats');
    heroElements.forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        setTimeout(() => {
            el.style.transition = 'opacity 0.8s ease-out, transform 0.8s ease-out';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, 100 + (index * 150));
    });

    // Animate hero visual
    const heroVisual = document.querySelector('.hero-visual');
    if (heroVisual) {
        heroVisual.style.opacity = '0';
        heroVisual.style.transform = 'translateY(40px)';
        setTimeout(() => {
            heroVisual.style.transition = 'opacity 1s ease-out, transform 1s ease-out';
            heroVisual.style.opacity = '1';
            heroVisual.style.transform = 'translateY(0)';
        }, 500);
    }

    // Start terminal animation
    runTerminalAnimation();
});

// ========================================
// Navbar Scroll Effect
// ========================================

const navbar = document.querySelector('.navbar');
let lastScrollY = window.scrollY;

window.addEventListener('scroll', () => {
    if (window.scrollY > 50) {
        navbar.classList.add('scrolled');
    } else {
        navbar.classList.remove('scrolled');
    }
    lastScrollY = window.scrollY;
});

// ========================================
// Mobile Menu Toggle
// ========================================

const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
const mobileMenu = document.querySelector('.mobile-menu');

mobileMenuBtn.addEventListener('click', () => {
    mobileMenu.classList.toggle('active');
    mobileMenuBtn.classList.toggle('active');
});

// Close mobile menu when clicking a link
document.querySelectorAll('.mobile-nav-links a').forEach(link => {
    link.addEventListener('click', () => {
        mobileMenu.classList.remove('active');
        mobileMenuBtn.classList.remove('active');
    });
});

// Close mobile menu when clicking outside
document.addEventListener('click', (e) => {
    if (!mobileMenu.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
        mobileMenu.classList.remove('active');
        mobileMenuBtn.classList.remove('active');
    }
});

// ========================================
// Terminal Typing Animation
// ========================================

const commands = [
    { cmd: 'launch spotify', output: ['Launching Spotify...', '<span class="success">✓ Spotify opened</span>'] },
    { cmd: 'weather new delhi', output: ['<span class="info">☀️ 28°C, Sunny</span>', 'Humidity: 45%', 'Wind: 12 km/h'] },
    { cmd: 'ai hello there!', output: ['<span class="success">Hello! How can I assist you today?</span>'] },
    { cmd: 'sysinfo', output: ['<span class="info">Device:</span> Android 14', '<span class="info">Battery:</span> 87%', '<span class="info">RAM:</span> 4.2GB free'] },
    { cmd: 'todo Learn Yantra', output: ['<span class="success">✓ Task added to your list</span>'] },
    { cmd: 'theme Tokyonight', output: ['<span class="success">✓ Theme applied successfully</span>'] },
    { cmd: 'quote', output: ['<span class="info">"The only way to do great work</span>', '<span class="info">is to love what you do."</span>', '— Steve Jobs'] },
];

let currentCommandIndex = 0;
const typedCommand = document.getElementById('typed-command');
const terminalOutput = document.getElementById('terminal-output');

async function typeCommand(text) {
    typedCommand.textContent = '';
    for (let i = 0; i < text.length; i++) {
        typedCommand.textContent += text[i];
        await sleep(50 + Math.random() * 50);
    }
}

async function showOutput(lines) {
    terminalOutput.innerHTML = '';
    for (const line of lines) {
        await sleep(100);
        const div = document.createElement('div');
        div.className = 'output-line';
        div.innerHTML = line;
        terminalOutput.appendChild(div);
    }
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTerminalAnimation() {
    while (true) {
        const { cmd, output } = commands[currentCommandIndex];

        // Clear previous output
        terminalOutput.innerHTML = '';

        // Type the command
        await typeCommand(cmd);

        // Wait a moment, then show output
        await sleep(500);
        await showOutput(output);

        // Wait before next command
        await sleep(3000);

        // Move to next command
        currentCommandIndex = (currentCommandIndex + 1) % commands.length;
    }
}

// Start terminal animation when page loads
document.addEventListener('DOMContentLoaded', () => {
    // Terminal animation is now started in the main DOMContentLoaded handler
});

// ========================================
// FAQ Accordion
// ========================================

const faqItems = document.querySelectorAll('.faq-item');

faqItems.forEach(item => {
    const question = item.querySelector('.faq-question');

    question.addEventListener('click', () => {
        // Close other items
        faqItems.forEach(otherItem => {
            if (otherItem !== item && otherItem.classList.contains('active')) {
                otherItem.classList.remove('active');
            }
        });

        // Toggle current item
        item.classList.toggle('active');
    });
});

// ========================================
// Smooth Scroll for Navigation Links
// ========================================

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            const headerOffset = 80;
            const elementPosition = target.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

            window.scrollTo({
                top: offsetPosition,
                behavior: 'smooth'
            });
        }
    });
});

// ========================================
// Intersection Observer for Stats Animation
// ========================================

const observerOptions = {
    threshold: 0.5,
    rootMargin: '0px'
};

const animateValue = (element, start, end, duration) => {
    const range = end - start;
    const startTime = performance.now();

    const updateValue = (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeProgress = 1 - Math.pow(1 - progress, 3); // easeOutCubic

        const current = Math.floor(start + (range * easeProgress));
        element.textContent = current + (element.dataset.suffix || '');

        if (progress < 1) {
            requestAnimationFrame(updateValue);
        }
    };

    requestAnimationFrame(updateValue);
};

// ========================================
// Parallax Effect for Background Orbs
// ========================================

let ticking = false;

window.addEventListener('scroll', () => {
    if (!ticking) {
        window.requestAnimationFrame(() => {
            const scrolled = window.pageYOffset;
            const orbs = document.querySelectorAll('.orb');

            orbs.forEach((orb, index) => {
                const speed = 0.05 + (index * 0.02);
                orb.style.transform = `translateY(${scrolled * speed}px)`;
            });

            ticking = false;
        });
        ticking = true;
    }
});

// ========================================
// Command Tags Hover Effect
// ========================================

const commandTags = document.querySelectorAll('.command-tag');

commandTags.forEach(tag => {
    tag.addEventListener('mouseenter', () => {
        tag.style.transform = 'translateY(-2px)';
    });

    tag.addEventListener('mouseleave', () => {
        tag.style.transform = 'translateY(0)';
    });
});

// ========================================
// Preloader / Page Load Animation
// ========================================

window.addEventListener('load', () => {
    document.body.classList.add('loaded');
});

// ========================================
// Easter Egg: Konami Code
// ========================================

const konamiCode = ['ArrowUp', 'ArrowUp', 'ArrowDown', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'ArrowLeft', 'ArrowRight', 'b', 'a'];
let konamiIndex = 0;

document.addEventListener('keydown', (e) => {
    if (e.key === konamiCode[konamiIndex]) {
        konamiIndex++;
        if (konamiIndex === konamiCode.length) {
            // Easter egg activated!
            document.body.style.setProperty('--color-primary', '#ff6b6b');
            document.body.style.setProperty('--color-primary-light', '#ff8787');
            document.body.style.setProperty('--color-accent', '#4ecdc4');

            // Reset after 5 seconds
            setTimeout(() => {
                document.body.style.removeProperty('--color-primary');
                document.body.style.removeProperty('--color-primary-light');
                document.body.style.removeProperty('--color-accent');
            }, 5000);

            konamiIndex = 0;
        }
    } else {
        konamiIndex = 0;
    }
});