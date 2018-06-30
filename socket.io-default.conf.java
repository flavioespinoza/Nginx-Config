upstream upstream-ubuntu {
        server  127.0.0.1:8080;
}

upstream upstream-nodejs {
        ip_hash;
        server 127.0.0.1:6001;
        server 127.0.0.1:6002;
        server 127.0.0.1:6003;
        server 127.0.0.1:6004;
        server 127.0.0.1:6005;
}

server {
        listen                  80;
        server_name             flavioespinoza.com www.flavioespinoza.com;
        rewrite                 ^(.*)   https://$host$1 permanent;
}

server {
        listen                  443 ssl;

        ssl                     on;
        server_name             flavioespinoza.com www.flavioespinoza.com;

        ssl_certificate /etc/letsencrypt/live/flavioespinoza.com/fullchain.pem; # managed by Certbot
        ssl_certificate_key /etc/letsencrypt/live/flavioespinoza.com/privkey.pem; # managed by Certbot
    
        include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
        ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot

        keepalive_timeout       60;
        ssl_session_cache       shared:SSL:10m;

        large_client_header_buffers 8 32k;

        location / {
                proxy_pass              http://upstream-ubuntu;
                proxy_next_upstream     error timeout invalid_header http_500 http_502 http_503 http_504;
                proxy_redirect          off;
                proxy_buffering         off;

                proxy_set_header        Host                    $host;
                proxy_set_header        X-Real-IP               $remote_addr;
                proxy_set_header        X-Forwarded-For         $proxy_add_x_forwarded_for;
                proxy_set_header        X-Forwarded-Proto       $scheme;
                add_header              Front-End-Https         on;
        }

        location /socket.io/ {
                proxy_pass              http://upstream-nodejs;
                proxy_redirect off;

                proxy_http_version      1.1;

                proxy_set_header        Upgrade                 $http_upgrade;
                proxy_set_header        Connection              "upgrade";

                proxy_set_header        Host                    $host;
                proxy_set_header        X-Real-IP               $remote_addr;
                proxy_set_header        X-Forwarded-For         $proxy_add_x_forwarded_for;
        }
}
