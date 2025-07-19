// src/app/core/services/domain/ticket/ticket-pdf.service.ts
import {inject, Injectable} from '@angular/core';
import {jsPDF} from 'jspdf';
import html2canvas from 'html2canvas';
import {from, Observable} from 'rxjs';
import * as QRCode from 'qrcode';
import {TicketPdfDataDto} from '../../../models/tickets/reservation.model';
import {TicketStatus} from '../../../models/tickets/ticket-status.enum';
import {NotificationService} from '../utilities/notification.service';

@Injectable({
  providedIn: 'root'
})
export class TicketPdfService {
  private notification = inject(NotificationService);

  /**
   * Génère un PDF pour un billet unique
   */
  generateSingleTicketPdf(pdfData: TicketPdfDataDto): Observable<void> {
    return from(this.createPdfFromTicketData(pdfData));
  }

  /**
   * Génère un PDF consolidé pour plusieurs billets
   */
  generateMultipleTicketsPdf(pdfDataArray: TicketPdfDataDto[]): Observable<void> {
    return from(this.createMultiplePdf(pdfDataArray));
  }

  private async createPdfFromTicketData(pdfData: TicketPdfDataDto): Promise<void> {
    try {
      // Générer le QR code
      const qrCodeDataUrl = await this.generateQRCode(pdfData.qrCodeValue);

      // Créer un élément HTML temporaire pour le billet
      const ticketElement = await this.createTicketHtmlElement(pdfData, qrCodeDataUrl);
      document.body.appendChild(ticketElement);

      // Attendre que les images se chargent
      await this.waitForImages(ticketElement);

      // Générer le canvas depuis l'HTML
      const canvas = await html2canvas(ticketElement, {
        // @ts-ignore
        scale: 2,
        useCORS: true,
        allowTaint: true,
        backgroundColor: '#ffffff'
      });

      // Nettoyer l'élément temporaire
      document.body.removeChild(ticketElement);

      // Créer le PDF
      const pdf = new jsPDF('p', 'mm', 'a4');
      const imgData = canvas.toDataURL('image/png');

      // Calculer les dimensions pour centrer le billet
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const imgWidth = 180; // Largeur du billet en mm
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      const x = (pdfWidth - imgWidth) / 2;
      const y = 20;

      pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight);

      // Télécharger le PDF
      const fileName = this.generateFileName(pdfData);
      pdf.save(fileName);

      this.notification.displayNotification('PDF généré avec succès !', 'valid');
    } catch (error) {
      console.error('Erreur lors de la génération du PDF:', error);
      this.notification.displayNotification('Erreur lors de la génération du PDF', 'error');
    }
  }

  private async createMultiplePdf(pdfDataArray: TicketPdfDataDto[]): Promise<void> {
    try {
      const pdf = new jsPDF('p', 'mm', 'a4');
      let isFirstPage = true;

      for (const pdfData of pdfDataArray) {
        if (!isFirstPage) {
          pdf.addPage();
        }

        // Générer le QR code pour ce billet
        const qrCodeDataUrl = await this.generateQRCode(pdfData.qrCodeValue);

        const ticketElement = await this.createTicketHtmlElement(pdfData, qrCodeDataUrl);
        document.body.appendChild(ticketElement);

        // Attendre que les images se chargent
        await this.waitForImages(ticketElement);

        const canvas = await html2canvas(ticketElement, {
        // @ts-ignore
          scale: 2,
          useCORS: true,
          allowTaint: true,
          backgroundColor: '#ffffff'
        });

        document.body.removeChild(ticketElement);

        const imgData = canvas.toDataURL('image/png');
        const pdfWidth = pdf.internal.pageSize.getWidth();
        const imgWidth = 180;
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
        const x = (pdfWidth - imgWidth) / 2;
        const y = 20;

        pdf.addImage(imgData, 'PNG', x, y, imgWidth, imgHeight);
        isFirstPage = false;
      }

      const eventName = pdfDataArray[0]?.eventSnapshot.name || 'Event';
      const fileName = `billets-${eventName.replace(/\s+/g, '-')}-${new Date().getTime()}.pdf`;
      pdf.save(fileName);

      this.notification.displayNotification(`${pdfDataArray.length} billets générés avec succès !`, 'valid');
    } catch (error) {
      console.error('Erreur lors de la génération du PDF multiple:', error);
      this.notification.displayNotification('Erreur lors de la génération du PDF', 'error');
    }
  }

  /**
   * Génère un QR code en base64
   */
  private async generateQRCode(value: string): Promise<string> {
    try {
      return await QRCode.toDataURL(value, {
        width: 160,
        margin: 1,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        },
        errorCorrectionLevel: 'M'
      });
    } catch (error) {
      console.error('Erreur lors de la génération du QR code:', error);
      // Retourner un placeholder en cas d'erreur
      return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjY2NjIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzMzMyIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkVycmV1ciBRUjwvdGV4dD48L3N2Zz4=';
    }
  }

  /**
   * Attend que toutes les images dans l'élément soient chargées
   */
  private async waitForImages(element: HTMLElement): Promise<void> {
    const images = element.querySelectorAll('img');
    const imagePromises = Array.from(images).map(img => {
      return new Promise<void>((resolve) => {
        if (img.complete) {
          resolve();
        } else {
          img.onload = () => resolve();
          img.onerror = () => resolve(); // Continue même en cas d'erreur
        }
      });
    });

    await Promise.all(imagePromises);
  }

  private async createTicketHtmlElement(pdfData: TicketPdfDataDto, qrCodeDataUrl: string): Promise<HTMLElement> {
    const ticketDiv = document.createElement('div');
    ticketDiv.style.cssText = `
      width: 600px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 20px;
      padding: 30px;
      color: white;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      box-shadow: 0 20px 40px rgba(0,0,0,0.1);
      position: relative;
      overflow: hidden;
    `;

    // Construire l'adresse complète à partir de l'objet address
    const fullAddress = this.buildFullAddress(pdfData.eventSnapshot.address);

    ticketDiv.innerHTML = `
      <div style="position: absolute; top: -50px; right: -50px; width: 150px; height: 150px;
                  background: rgba(255,255,255,0.1); border-radius: 50%; z-index: 1;"></div>
      <div style="position: absolute; bottom: -30px; left: -30px; width: 100px; height: 100px;
                  background: rgba(255,255,255,0.1); border-radius: 50%; z-index: 1;"></div>

      <div style="position: relative; z-index: 2;">
        <!-- En-tête avec logo structure -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
          <div>
            <h1 style="margin: 0; font-size: 24px; font-weight: bold;">${pdfData.eventSnapshot.name}</h1>
            <p style="margin: 5px 0 0 0; opacity: 0.9; font-size: 14px;">${pdfData.structureName || 'Structure'}</p>
          </div>
          ${pdfData.organizerLogoUrl ?
      `<img src="${pdfData.organizerLogoUrl}" alt="Logo" style="width: 100px; height: 100px; border-radius: 10px; object-fit: cover;" crossorigin="anonymous">` :
      '<div style="width: 100px; height: 100px; background: rgba(255,255,255,0.2); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 24px;">🎫</div>'
    }
        </div>

        <!-- Image de l'événement -->
        ${pdfData.eventSnapshot.mainPhotoUrl ?
      `<div style="margin-bottom: 25px; border-radius: 15px; overflow: hidden;">
            <img src="${pdfData.eventSnapshot.mainPhotoUrl}" alt="Event" style="width: 100%; height: 200px; object-fit: cover; object-position: center;" crossorigin="anonymous">
          </div>` : ''
    }

        <!-- Informations principales -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px;">
          <div>
            <h3 style="margin: 0 0 10px 0; font-size: 16px; opacity: 0.8;">Participant</h3>
            <p style="margin: 0; font-size: 18px; font-weight: bold;">
              ${pdfData.participant.firstName} ${pdfData.participant.lastName}
            </p>
          </div>
          <div>
            <h3 style="margin: 0 0 10px 0; font-size: 16px; opacity: 0.8;">Date & Heure</h3>
            <p style="margin: 0; font-size: 16px;">
              ${this.formatDate(pdfData.eventSnapshot.startDate)}
            </p>
          </div>
        </div>

        <!-- Zone audience et lieu -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 25px;">
          <div>
            <h3 style="margin: 0 0 10px 0; font-size: 16px; opacity: 0.8;">Zone</h3>
            <p style="margin: 0; font-size: 16px;">${pdfData.audienceZoneSnapshot?.name || 'Zone générale'}</p>
          </div>
          <div>
            <h3 style="margin: 0 0 10px 0; font-size: 16px; opacity: 0.8;">Lieu</h3>
            <p style="margin: 0; font-size: 16px;">${fullAddress || 'À préciser'}</p>
          </div>
        </div>

        <!-- Code QR et ID billet -->
        <div style="display: flex; justify-content: space-between; align-items: center;
                    background: rgba(255,255,255,0.1); border-radius: 15px; padding: 20px; margin-bottom: 20px;">
          <div>
            <h3 style="margin: 0 0 10px 0; font-size: 16px; opacity: 0.8;">ID Billet</h3>
            <p style="margin: 0; font-size: 20px; font-weight: bold; font-family: monospace;">
              ${pdfData.id.slice(-8).toUpperCase()}
            </p>
            <p style="margin: 5px 0 0 0; font-size: 14px; opacity: 0.8;">
              Statut: ${this.getStatusLabel(pdfData.status)}
            </p>
          </div>
          <div style="background: white; padding: 10px; border-radius: 10px;">
            <img src="${qrCodeDataUrl}" alt="QR Code" style="width: 160px; height: 160px; display: block;">
          </div>
        </div>

        <!-- Conditions -->
        <div style="font-size: 12px; opacity: 0.8; line-height: 1.4;">
          ${pdfData.termsAndConditions || 'Billet non remboursable, non échangeable. Présentez ce billet à l\'entrée.'}
        </div>
      </div>
    `;

    return ticketDiv;
  }

  private buildFullAddress(address: { street: string; city: string; zipCode: string; country: string }): string {
    if (!address) return '';

    const parts = [];
    if (address.street) parts.push(address.street);
    if (address.zipCode && address.city) {
      parts.push(`${address.zipCode} ${address.city}`);
    } else if (address.city) {
      parts.push(address.city);
    }
    if (address.country) parts.push(address.country);

    return parts.join(', ');
  }

  private formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private getStatusLabel(status: TicketStatus): string {
    const statusLabels: { [key in TicketStatus]: string } = {
      [TicketStatus.VALID]: 'Valide',
      [TicketStatus.USED]: 'Utilisé',
      [TicketStatus.CANCELLED]: 'Annulé',
      [TicketStatus.EXPIRED]: 'Expiré'
    };
    return statusLabels[status] || status;
  }

  private generateFileName(pdfData: TicketPdfDataDto): string {
    const eventName = pdfData.eventSnapshot.name.replace(/\s+/g, '-');
    const participantName = `${pdfData.participant.firstName}-${pdfData.participant.lastName}`;
    const ticketId = pdfData.id.slice(-6);
    return `billet-${eventName}-${participantName}-${ticketId}.pdf`;
  }
}
